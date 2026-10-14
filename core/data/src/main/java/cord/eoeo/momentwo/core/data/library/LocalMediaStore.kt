package cord.eoeo.momentwo.core.data.library

import android.util.Log
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import cord.eoeo.momentwo.core.database.MomentwoDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Owned originals are in filesDir, never Coil/cacheDir. All imported files are validated before acceptance. */
@Singleton
class LocalMediaStore @Inject constructor(@ApplicationContext private val context: Context, private val db: MomentwoDatabase) {
    private val lock = Mutex()
    private val directory get() = File(context.filesDir, "library-media")
    fun file(name: String): File {
        require(name.matches(Regex("[a-zA-Z0-9_-]+\\.[a-zA-Z0-9]+"))) { "Invalid media reference" }
        return File(directory, name)
    }
    private suspend fun import(source: Uri): String = withContext(Dispatchers.IO) {
        if (!directory.exists() && !directory.mkdirs()) throw IOException("사진 저장 공간을 만들 수 없습니다")
        val temp = File(directory, ".import-${UUID.randomUUID()}")
        var saved: File? = null
        try {
            val input = context.contentResolver.openInputStream(source) ?: throw IOException("사진을 읽을 수 없습니다")
            input.use { stream -> temp.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) { currentCoroutineContext().ensureActive(); val size = stream.read(buffer); if (size < 0) break; output.write(buffer, 0, size) }
                output.fd.sync()
            } }
            currentCoroutineContext().ensureActive()
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(temp.path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("지원하는 사진 파일을 선택해주세요")
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(bounds.outMimeType) ?: "img"
            val name = "${UUID.randomUUID()}.$extension"
            saved = file(name)
            if (!temp.renameTo(saved)) throw IOException("사진을 보관하지 못했습니다")
            currentCoroutineContext().ensureActive()
            name
        } catch (failure: Throwable) { temp.delete(); saved?.delete(); throw failure }
    }
    suspend fun <T> withImported(source: Uri, commit: suspend (String) -> T): T = lock.withLock {
        var name: String? = null
        try { name = import(source); commit(name) }
        finally {
            // A cancellation can arrive after SQLite commits but before the caller resumes.
            // Decide compensation from the actual DB references, never a local boolean.
            withContext(NonCancellable + Dispatchers.IO) {
                try { if (name != null && name !in db.libraryDao().mediaReferences()) file(name).delete() }
                catch (failure: Exception) { Log.e("LocalMedia", "Compensation deferred to cleanup", failure) }
            }
        }
    }
    suspend fun cleanUnused() = lock.withLock {
        withContext(Dispatchers.IO) {
            val references = db.libraryDao().mediaReferences().toSet()
            directory.listFiles()?.forEach { if (it.name.startsWith(".import-") || it.name !in references) it.delete() }
        }
    }
    suspend fun export(name: String): Uri = exportFile(file(name), name)
    internal suspend fun exportFile(source: File, name: String): Uri = withContext(Dispatchers.IO) {
        if (!source.isFile) throw IOException("기기에 사진 파일이 없습니다")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.path, bounds)
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, bounds.outMimeType ?: "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Momentwo")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val target = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: throw IOException("갤러리에 저장할 수 없습니다")
        var published = false
        try {
            source.inputStream().use { input -> (context.contentResolver.openOutputStream(target) ?: throw IOException("갤러리에 저장할 수 없습니다")).use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) { currentCoroutineContext().ensureActive(); val n = input.read(buffer); if (n < 0) break; output.write(buffer, 0, n) }
            } }
            currentCoroutineContext().ensureActive()
            context.contentResolver.update(target, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            published = true
            target
        } finally { if (!published) withContext(NonCancellable) { context.contentResolver.delete(target, null, null) } }
    }
}
