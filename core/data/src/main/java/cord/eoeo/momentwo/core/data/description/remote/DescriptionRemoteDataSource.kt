package cord.eoeo.momentwo.core.data.description.remote

import cord.eoeo.momentwo.core.data.description.DescriptionDataSource
import cord.eoeo.momentwo.core.data.model.CreateDescription
import cord.eoeo.momentwo.core.data.model.Description
import cord.eoeo.momentwo.core.data.model.EditDescription
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DescriptionRemoteDataSource(
    private val descriptionService: DescriptionService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DescriptionDataSource {
    override suspend fun createDescription(createDescription: CreateDescription): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                descriptionService.postCreateDescription(createDescription)
            }
        }

    override suspend fun editDescription(editDescription: EditDescription): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                descriptionService.putEditDescription(editDescription)
            }
        }

    override suspend fun deleteDescription(
        albumId: Int,
        photoId: Int,
    ): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                descriptionService.deleteDescription(albumId, photoId)
            }
        }

    override suspend fun getDescription(
        albumId: Int,
        photoId: Int,
    ): Result<Description> =
        runCatching {
            withContext(dispatcher) {
                descriptionService.getDescription(albumId, photoId)
            }
        }
}
