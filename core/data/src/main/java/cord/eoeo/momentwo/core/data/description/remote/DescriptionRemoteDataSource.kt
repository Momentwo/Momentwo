package cord.eoeo.momentwo.core.data.description.remote

import cord.eoeo.momentwo.core.common.runSuspendCatching

import cord.eoeo.momentwo.core.data.description.DescriptionDataSource
import cord.eoeo.momentwo.core.data.model.CreateDescription
import cord.eoeo.momentwo.core.data.model.Description
import cord.eoeo.momentwo.core.data.model.EditDescription
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DescriptionRemoteDataSource(
    private val descriptionService: DescriptionService,
    private val dispatcher: CoroutineDispatcher,
) : DescriptionDataSource {
    override suspend fun createDescription(createDescription: CreateDescription): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                descriptionService.postCreateDescription(createDescription)
            }
        }

    override suspend fun editDescription(editDescription: EditDescription): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                descriptionService.putEditDescription(editDescription)
            }
        }

    override suspend fun deleteDescription(
        albumId: Int,
        photoId: Int,
    ): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                descriptionService.deleteDescription(albumId, photoId)
            }
        }

    override suspend fun getDescription(
        albumId: Int,
        photoId: Int,
    ): Result<Description> =
        runSuspendCatching {
            withContext(dispatcher) {
                descriptionService.getDescription(albumId, photoId)
            }
        }
}
