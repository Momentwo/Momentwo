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
class LocalAlbumMediaTest {
    @Test fun coverIsDurableCacheIndependentAndFailedImportKeepsOldCover() = runBlocking {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(target.filesDir, "media-test-${UUID.randomUUID()}").apply { mkdirs() }
        val context = object : ContextWrapper(target) { override fun getFilesDir() = root }
        val db = Room.inMemoryDatabaseBuilder(target, MomentwoDatabase::class.java).build()
        try {
            val source = File(root, "source.png")
            Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888).also { bitmap -> source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle() }
            val original = source.readBytes()
            val repository = RoomLocalLibraryRepository(db, context)
            val album = repository.createAlbum("Cover")
            repository.setCover(album, source.toUri())
            val cover = repository.album(album).first()!!.coverUri
            val owned = File(requireNotNull(android.net.Uri.parse(cover).path))
            assertTrue(owned.isFile)
            assertArrayEquals(original, source.readBytes())
            assertArrayEquals(original, owned.readBytes())
            val invalid = File(root, "invalid.txt").apply { writeText("not an image") }
            try { repository.setCover(album, invalid.toUri()); fail("Expected validation failure") } catch (_: IOException) { }
            assertEquals(cover, repository.album(album).first()!!.coverUri)
            assertTrue(owned.exists())
            repository.setCover(album, null)
            assertFalse(owned.exists())
            assertTrue(source.exists())
        } finally { db.close(); root.deleteRecursively() }
    }
    @Test fun cancellationAfterDbCommitCannotDeleteReferencedOriginal() = runBlocking {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(target.filesDir, "media-cancel-${UUID.randomUUID()}").apply { mkdirs() }
        val context = object : ContextWrapper(target) { override fun getFilesDir() = root }
        val db = Room.inMemoryDatabaseBuilder(target, MomentwoDatabase::class.java).build()
        try {
            val source = File(root, "source.png")
            Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).also { bitmap -> source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle() }
            val repository = RoomLocalLibraryRepository(db, context)
            val album = repository.createAlbum("Committed")
            val store = LocalMediaStore(context, db)
            var mediaName: String? = null
            val job = launch {
                store.withImported(source.toUri()) { name ->
                    mediaName = name
                    db.libraryDao().updateAlbum(db.libraryDao().album(album)!!.copy(coverMediaName = name))
                    currentCoroutineContext().cancel(); yield()
                }
            }
            job.join(); assertTrue(job.isCancelled)
            assertTrue(store.file(requireNotNull(mediaName)).isFile)
            assertEquals(mediaName, db.libraryDao().album(album)!!.coverMediaName)
        } finally { db.close(); root.deleteRecursively() }
    }
}
