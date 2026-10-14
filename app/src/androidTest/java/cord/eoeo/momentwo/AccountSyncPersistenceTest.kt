package cord.eoeo.momentwo

import android.content.ContextWrapper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.database.*
import cord.eoeo.momentwo.core.datastore.PreferenceRepositoryImpl
import cord.eoeo.momentwo.core.model.*
import java.io.File
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountSyncPersistenceTest {
    @get:Rule val migrations=MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),MomentwoDatabase::class.java)
    @Test fun v3UpgradePreservesDeviceDataAndOnlyLinksOwnedAccountRows() {
        val name="sync-migration-${UUID.randomUUID()}.db"
        migrations.createDatabase(name,3).apply {
            execSQL("INSERT INTO library VALUES ('device',NULL,'이 기기'),('account:A','A','A')")
            execSQL("INSERT INTO local_album(localId,libraryId,title,subtitle,revision,createdAt) VALUES ('local','device','original','note',4,1)")
            execSQL("INSERT INTO local_album(localId,libraryId,title,subtitle,remoteId,revision,createdAt) VALUES ('remote','account:A','server','',7,0,1)")
            execSQL("INSERT INTO local_subalbum(localId,albumId,title,revision,createdAt) VALUES ('child','local','sub',0,1)")
            execSQL("INSERT INTO local_photo(localId,subAlbumId,mediaName,remoteUrl,description,descriptionKnown,favorite,isLiked,revision,createdAt) VALUES ('photo','child','owned.png','','private',1,1,0,2,1)")
            execSQL("INSERT INTO local_memo VALUES ('memo','photo','only me',1)");close()
        }
        migrations.runMigrationsAndValidate(name,4,true,MIGRATION_3_4).apply {
            query("SELECT description,favorite,mediaName FROM local_photo WHERE localId='photo'").use{assertTrue(it.moveToFirst());assertEquals("private",it.getString(0));assertEquals(1,it.getInt(1));assertEquals("owned.png",it.getString(2))}
            query("SELECT accountId,remoteId FROM sync_link").use{assertEquals(1,it.count);it.moveToFirst();assertEquals("A",it.getString(0));assertEquals(7,it.getInt(1))}
            query("SELECT COUNT(*) FROM local_memo").use{it.moveToFirst();assertEquals(1,it.getInt(0))};close()
        }
    }
    @Test fun offlineMutationsCoalesceAndCancellationPreservesIndependentFavorite()=fixture { db,repo,sync,prefs ->
        val library="account:A";db.libraryDao().insertLibrary(LibraryEntity(library,"A","A"))
        db.libraryDao().insertAlbum(LocalAlbumEntity("album",library,"Remote",remoteId=1,role=MemberAuth.ADMIN.roleString,createdAt=0))
        db.libraryDao().insertSubAlbum(LocalSubAlbumEntity("sub","album","Sub",remoteId=2,createdAt=0))
        db.libraryDao().insertPhoto(LocalPhotoEntity("photo","sub",null,remoteId=3,description="base",createdAt=0))
        db.syncDao().putLink(SyncLinkEntity("photo","A","PHOTO","sub",3))
        repo.setDescription("photo","first");repo.setDescription("photo","latest");repo.setFavorite("photo",true)
        val job=db.syncDao().jobs("A").single();assertEquals("base",job.baseText);assertEquals("latest",job.text)
        sync.cancel(job.id,prefs.getSession().getOrThrow()!!.identity)
        val photo=repo.photo("photo").first()!!;assertEquals("base",photo.description);assertTrue(photo.favorite)
        assertEquals("DONE",db.syncDao().job(job.id)!!.status)
        val draft=sync.createAccountAlbum("Unsent","draft")
        val child=repo.createSubAlbum(draft,"New","new-sub")
        repo.deleteAlbum(draft)
        assertNull(db.libraryDao().album(draft));assertNull(db.libraryDao().subAlbum(child))
        assertTrue(db.syncDao().jobs("A").filter{it.targetId in setOf(draft,child)}.all{it.status=="CANCELLED"})
        prefs.storeSession("B","b","r");assertNull(repo.photo("photo").first());assertTrue(sync.observeWorks().first().isEmpty())
        assertNotNull(db.libraryDao().photo("photo"))
    }
    @Test fun cancellingCopyNeverDeletesOriginalOrUploadsPrivateNotes()=fixture { db,repo,sync,prefs ->
        val original=repo.createAlbum("Device");val child=repo.createSubAlbum(original,"Child")
        sync.copyAlbums(setOf(original),prefs.getSession().getOrThrow()!!.identity,"copy")
        val replica=repo.albums(LibraryScope.Account("A")).first().single().localId
        val job=db.syncDao().jobs("A").first()
        sync.cancel(job.id,prefs.getSession().getOrThrow()!!.identity)
        assertNotNull(repo.album(original).first());assertNotNull(repo.subAlbum(child).first());assertNull(db.libraryDao().album(replica))
        assertTrue(db.syncDao().jobs("A").all{it.status=="CANCELLED"})
    }
    private fun fixture(block:suspend(MomentwoDatabase,RoomLocalLibraryRepository,AccountSyncRepository,PreferenceRepositoryImpl)->Unit)=runBlocking {
        val target=InstrumentationRegistry.getInstrumentation().targetContext
        val root=File(target.filesDir,"sync-test-${UUID.randomUUID()}").apply{mkdirs()}
        val context=object:ContextWrapper(target){override fun getFilesDir()=root}
        val db=Room.inMemoryDatabaseBuilder(context,MomentwoDatabase::class.java).build()
        val values=MutableStateFlow<Preferences>(emptyPreferences())
        val prefs=PreferenceRepositoryImpl(object:DataStore<Preferences>{override val data:Flow<Preferences> = values;override suspend fun updateData(transform:suspend(Preferences)->Preferences)=transform(values.value).also{values.value=it}})
        prefs.storeSession("A","a","r")
        val media=LocalMediaStore(context,db);val sync=AccountSyncRepository(db,prefs,media,object:SyncScheduling{override fun enqueue(){}})
        try{block(db,RoomLocalLibraryRepository(db,context,media,prefs,sync),sync,prefs)}finally{db.close();root.deleteRecursively()}
    }
}
