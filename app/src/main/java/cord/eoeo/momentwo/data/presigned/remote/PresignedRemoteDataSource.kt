package cord.eoeo.momentwo.data.presigned.remote

import cord.eoeo.momentwo.data.presigned.PresignedDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody

class PresignedRemoteDataSource(
    private val presignedService: PresignedService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): PresignedDataSource {
    override suspend fun uploadPhoto(presignedUrl: String, image: RequestBody): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                presignedService.uploadImage(presignedUrl, image)
            }
        }
}
