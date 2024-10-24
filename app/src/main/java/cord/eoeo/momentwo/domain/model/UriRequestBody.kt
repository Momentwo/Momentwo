package cord.eoeo.momentwo.domain.model

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

class UriRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
) : RequestBody() {
    private var fileSize: Long = -1L

    init {
        uri
        contentResolver.query(
            uri,
            arrayOf(MediaStore.Images.Media.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            cursor.moveToFirst()
            fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
        }
    }

    override fun contentLength(): Long = fileSize

    override fun contentType(): MediaType? = contentResolver.getType(uri)?.toMediaTypeOrNull()

    override fun writeTo(sink: BufferedSink) {
        contentResolver.openInputStream(uri)?.source()?.use { source ->
            sink.writeAll(source)
        }
    }
}
