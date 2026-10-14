package cord.eoeo.momentwo
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.room.Room
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
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class AccountCopyStorageTest {
    @Test fun selectedSnapshotPreservesOriginalAndPrivateDataAndSurvivesSourceDeletionAndReopen()=runBlocking {
        val target=InstrumentationRegistry.getInstrumentation().targetContext
        val root=File(target.filesDir,"copy-test-${UUID.randomUUID()}").apply{mkdirs()}
        val context=object:ContextWrapper(target){override fun getFilesDir()=root}
        val filename=File(root,"copy.db").path
        fun open()=Room.databaseBuilder(target,MomentwoDatabase::class.java,filename).build()
        var db=open();val preferences=preferences();preferences.storeSession("A","fixture-a","fixture-r")
        var schedules=0;val scheduler=object:SyncScheduling{override fun enqueue(){schedules++}}
        try {
            var media=LocalMediaStore(context,db);var sync=AccountSyncRepository(db,preferences,media,scheduler);var repo=RoomLocalLibraryRepository(db,context,media,preferences,sync)
            val source=File(root,"source.png");Bitmap.createBitmap(8,8,Bitmap.Config.ARGB_8888).also{it.eraseColor(android.graphics.Color.RED);source.outputStream().use{out->it.compress(Bitmap.CompressFormat.PNG,100,out)};it.recycle()}
            val original=source.readBytes();val album=repo.createAlbum("Source");val other=repo.createAlbum("Not selected");val sub=repo.createSubAlbum(album,"Child");val photo=repo.importPhoto(sub,source.toUri())
            repo.setDescription(photo,"before");repo.setFavorite(photo,true);repo.saveMemo(photo,"private")
            val identity=preferences.getSession().getOrThrow()!!.identity
            sync.copyAlbums(setOf(album),identity,"intent");sync.copyAlbums(setOf(album),identity,"intent")
            val replica=repo.albums(LibraryScope.Account("A")).first().single();assertEquals("Source",replica.title);assertNull(replica.role);assertTrue(replica.copying);assertNotEquals(album,replica.localId)
            val child=repo.subAlbums(replica.localId).first().single();val copied=repo.photos(child.localId).first().single()
            assertEquals("before",copied.description);assertFalse(copied.favorite);assertTrue(repo.memos(copied.localId).first().isEmpty());assertNull(copied.remoteId)
            assertTrue(repo.photo(photo).first()!!.favorite);assertEquals("private",repo.memos(photo).first().single().text)
            repo.editAlbum(album,"After","changed");repo.setDescription(photo,"after")
            assertEquals("Source",repo.album(replica.localId).first()!!.title);assertEquals("before",repo.photo(copied.localId).first()!!.description)
            assertEquals(setOf(SyncKind.CREATE_ALBUM.name,SyncKind.CREATE_SUB.name,SyncKind.SET_SUBTITLE.name,SyncKind.UPLOAD_PHOTO.name,SyncKind.SET_DESCRIPTION.name),db.syncDao().jobs("A").map{it.kind}.toSet())
            assertTrue(db.syncDao().jobs("B").isEmpty());assertTrue(repo.album(other).first()!=null)
            repo.deleteAlbum(album);assertArrayEquals(original,media.file(copied.mediaName!!).readBytes());assertArrayEquals(original,source.readBytes())
            db.close();db=open();media=LocalMediaStore(context,db);sync=AccountSyncRepository(db,preferences,media,scheduler);repo=RoomLocalLibraryRepository(db,context,media,preferences,sync)
            assertNotNull(db.syncDao().batch("intent"));assertEquals(5,db.syncDao().jobs("A").size);assertArrayEquals(original,media.file(copied.mediaName!!).readBytes())
            preferences.clearSession();assertTrue(repo.albums(LibraryScope.Account("A")).first().isEmpty());assertNotNull(db.libraryDao().album(replica.localId));assertNotNull(repo.album(other).first())
            assertEquals(2,schedules)
        } finally {db.close();root.deleteRecursively()}
    }
    @Test fun missingSourceOrChangedAccountCannotProducePartialCopy()=runBlocking {
        val target=InstrumentationRegistry.getInstrumentation().targetContext;val root=File(target.filesDir,"copy-failure-${UUID.randomUUID()}").apply{mkdirs()};val context=object:ContextWrapper(target){override fun getFilesDir()=root};val db=Room.inMemoryDatabaseBuilder(target,MomentwoDatabase::class.java).build()
        val preferences=preferences();preferences.storeSession("A","a","r");val scheduler=object:SyncScheduling{override fun enqueue(){fail("No successful snapshot")}}
        try {
            val media=LocalMediaStore(context,db);val sync=AccountSyncRepository(db,preferences,media,scheduler);val repo=RoomLocalLibraryRepository(db,context,media,preferences,sync)
            val source=repo.createAlbum("Source");val identity=preferences.getSession().getOrThrow()!!.identity
            try{sync.copyAlbums(setOf(source,"missing"),identity,"failed");fail("Expected missing original")}catch(_:IllegalArgumentException){}
            assertNull(db.syncDao().batch("failed"));assertTrue(db.syncDao().jobs("A").isEmpty());assertTrue(repo.albums(LibraryScope.Account("A")).first().isEmpty())
            val child=repo.createSubAlbum(source,"Child")
            val missingPhoto=UUID.randomUUID().toString()
            db.libraryDao().insertPhoto(LocalPhotoEntity(missingPhoto,child,"${UUID.randomUUID()}.png",createdAt=0))
            try{sync.copyAlbums(setOf(source),identity,"missing-file");fail("Expected unavailable owned file")}catch(_:IllegalStateException){}
            assertNull(db.syncDao().batch("missing-file"));assertTrue(db.syncDao().jobs("A").isEmpty());assertTrue(repo.albums(LibraryScope.Account("A")).first().isEmpty())
            assertNotNull(db.libraryDao().photo(missingPhoto));assertNotNull(repo.subAlbum(child).first())
            preferences.storeSession("B","b","r");try{sync.copyAlbums(setOf(source),identity,"wrong-account");fail("Expected changed recipient")}catch(_:IllegalStateException){}
            assertNotNull(repo.album(source).first());assertNull(db.syncDao().batch("wrong-account"));assertTrue(db.syncDao().jobs("B").isEmpty())
        } finally{db.close();root.deleteRecursively()}
    }
    private fun preferences():PreferenceRepositoryImpl {val values=MutableStateFlow<Preferences>(emptyPreferences());return PreferenceRepositoryImpl(object:DataStore<Preferences>{override val data:Flow<Preferences> = values;override suspend fun updateData(transform:suspend(Preferences)->Preferences)=transform(values.value).also{values.value=it}})}
}
