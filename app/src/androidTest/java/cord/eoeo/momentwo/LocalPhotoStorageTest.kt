package cord.eoeo.momentwo
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.database.*
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class LocalPhotoStorageTest {
    @Test fun importedOriginalAndPrivateRecordsSurviveUriRevocationCacheCleanupAndDbReopen() = runBlocking {
        val target=InstrumentationRegistry.getInstrumentation().targetContext
        val root=File(target.filesDir,"photo-test-${UUID.randomUUID()}").apply{mkdirs()}
        val context=object:ContextWrapper(target){override fun getFilesDir()=root;override fun getCacheDir()=File(root,"cache")}
        val database=File(root,"test.db")
        fun open()=Room.databaseBuilder(target,MomentwoDatabase::class.java,database.path).build()
        var db=open()
        try{
            val source=File(root,"source.png");Bitmap.createBitmap(8,8,Bitmap.Config.ARGB_8888).also{b->source.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
            val original=source.readBytes();var repo=RoomLocalLibraryRepository(db,context)
            val album=repo.createAlbum("local");val sub=repo.createSubAlbum(album,"sub")
            val photo=repo.importPhoto(sub,source.toUri(),"photo-intent")
            repo.importPhoto(sub,source.toUri(),"photo-intent");assertEquals(1,repo.photos(sub).first().size)
            repo.setDescription(photo,"caption");repo.setFavorite(photo,true);repo.saveMemo(photo,"private","memo-intent");repo.saveMemo(photo,"edited private","memo-intent")
            assertArrayEquals(original,source.readBytes());source.delete();context.cacheDir.mkdirs();File(context.cacheDir,"cached").writeText("evict");context.cacheDir.deleteRecursively();repo.cleanUnusedMedia()
            db.close();db=open();repo=RoomLocalLibraryRepository(db,context)
            val restored=repo.photo(photo).first()!!;assertEquals("caption",restored.description);assertTrue(restored.favorite)
            assertEquals("edited private",repo.memos(photo).first().single().text)
            assertArrayEquals(original,File(android.net.Uri.parse(restored.imageUri).path!!).readBytes())
            val gallery=LocalMediaStore(context,db).export(restored.mediaName!!)
            try{assertArrayEquals(original,target.contentResolver.openInputStream(gallery)!!.use{it.readBytes()})}finally{target.contentResolver.delete(gallery,null,null)}
            repo.deletePhoto(photo);assertNull(repo.photo(photo).first());assertTrue(repo.memos(photo).first().isEmpty());assertTrue(db.libraryDao().mediaReferences().isEmpty())
        }finally{db.close();root.deleteRecursively()}
    }
    @Test fun failedCommitAndMissingUriDoNotLeaveAcceptedPhotosOrDestroySource()=runBlocking{
        val target=InstrumentationRegistry.getInstrumentation().targetContext
        val root=File(target.filesDir,"photo-fail-${UUID.randomUUID()}").apply{mkdirs()}
        val context=object:ContextWrapper(target){override fun getFilesDir()=root;override fun getCacheDir()=File(root,"cache")};val db=Room.inMemoryDatabaseBuilder(target,MomentwoDatabase::class.java).build()
        try{
            val source=File(root,"source.png");Bitmap.createBitmap(4,4,Bitmap.Config.ARGB_8888).also{b->source.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
            val media=LocalMediaStore(context,db)
            try{media.withImported(source.toUri()){throw IOException("database full")};fail("Expected commit failure")}catch(_:IOException){}
            assertTrue(source.isFile);assertTrue(File(root,"library-media").listFiles()!!.isEmpty())
            val repo=RoomLocalLibraryRepository(db,context);val sub=repo.createSubAlbum(repo.createAlbum("a"),"s")
            try{repo.importPhoto(sub,File(root,"missing.png").toUri());fail("Expected missing URI")}catch(_:IOException){}
            assertTrue(repo.photos(sub).first().isEmpty());assertTrue(source.isFile)
        }finally{db.close();root.deleteRecursively()}
    }
}
