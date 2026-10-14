package cord.eoeo.momentwo
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.database.*
import cord.eoeo.momentwo.core.datastore.PreferenceRepositoryImpl
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.model.LibraryScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class AccountStorageTest{
    @get:Rule val migrations=MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),MomentwoDatabase::class.java)
    @Test fun v2DeviceOriginalsSurviveAccountCacheMigration(){
        val name="account-migration-${java.util.UUID.randomUUID()}.db";migrations.createDatabase(name,2).apply{
            execSQL("INSERT INTO library VALUES ('device',NULL,'이 기기')")
            execSQL("INSERT INTO local_album VALUES ('owned','device','title','subtitle','owned.png',NULL,2,10)")
            execSQL("INSERT INTO friend VALUES ('unowned','image')");close()
        }
        migrations.runMigrationsAndValidate(name,3,true,MIGRATION_2_3).apply{
            query("SELECT title,subtitle,coverMediaName,role,subsLoaded FROM local_album WHERE localId='owned'").use{assertTrue(it.moveToFirst());assertEquals("title",it.getString(0));assertEquals("subtitle",it.getString(1));assertEquals("owned.png",it.getString(2));assertTrue(it.isNull(3));assertEquals(1,it.getInt(4))}
            query("SELECT COUNT(*) FROM friend").use{it.moveToFirst();assertEquals(0,it.getInt(0))};close()
        }
    }
    @Test fun sameServerIdsAndNicknamesAreIsolatedAndLogoutCannotEraseDeviceData()=runBlocking{
        val context=InstrumentationRegistry.getInstrumentation().targetContext;val db=Room.inMemoryDatabaseBuilder(context,MomentwoDatabase::class.java).build()
        val values=MutableStateFlow<Preferences>(emptyPreferences());val preferences=PreferenceRepositoryImpl(object:DataStore<Preferences>{override val data:Flow<Preferences> = values;override suspend fun updateData(transform:suspend(Preferences)->Preferences)=transform(values.value).also{values.value=it}})
        try{
            val repo=RoomLocalLibraryRepository(db,context,LocalMediaStore(context,db),preferences);val device=repo.createAlbum("original")
            db.libraryDao().insertLibrary(LibraryEntity("account:A","A","A"));db.libraryDao().insertLibrary(LibraryEntity("account:B","B","B"))
            db.libraryDao().insertAlbum(LocalAlbumEntity("A-album","account:A","A",remoteId=1,createdAt=0));db.libraryDao().insertAlbum(LocalAlbumEntity("B-album","account:B","B",remoteId=1,createdAt=0))
            db.friendDao().insertAll(FriendEntity("same","A","A"),FriendEntity("same","B","B"))
            db.photoDao().insertAll(PhotoEntity(1,1,1,"A",false,"A"),PhotoEntity(1,1,1,"B",false,"B"));db.photoDao().updateIsLiked(1,true,"A")
            db.openHelper.writableDatabase.query("SELECT is_liked FROM photo WHERE ownerId='B' AND id=1").use{assertTrue(it.moveToFirst());assertEquals(0,it.getInt(0))}
            assertEquals("A",db.friendDao().getFriendList("A").single().userProfileImage);db.friendDao().deleteAll("A");assertEquals("B",db.friendDao().getFriendList("B").single().userProfileImage)
            preferences.storeSession("A","a","r");assertEquals("A-album",repo.albums(LibraryScope.Account("A")).first().single().localId);assertNull(repo.album("B-album").first())
            preferences.storeSession("B","b","r");assertTrue(repo.albums(LibraryScope.Account("A")).first().isEmpty());assertNull(repo.album("A-album").first());assertEquals("B-album",repo.album("B-album").first()!!.localId)
            preferences.clearSession();assertNull(repo.album("B-album").first());assertEquals("original",repo.album(device).first()!!.title)
            assertNotNull(db.libraryDao().album("A-album"));assertNotNull(db.libraryDao().album("B-album"))
        }finally{db.close()}
    }
}
