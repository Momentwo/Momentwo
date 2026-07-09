package cord.eoeo.momentwo.core.data.presigned

import okhttp3.RequestBody

interface PresignedDataSource {
    suspend fun uploadPhoto(presignedUrl: String, image: RequestBody): Result<Unit>
}
