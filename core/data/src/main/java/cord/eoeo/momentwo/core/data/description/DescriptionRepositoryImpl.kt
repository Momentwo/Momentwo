package cord.eoeo.momentwo.core.data.description

import cord.eoeo.momentwo.core.data.model.CreateDescription
import cord.eoeo.momentwo.core.data.model.EditDescription
import cord.eoeo.momentwo.core.model.DescriptionItem

class DescriptionRepositoryImpl(
    private val descriptionRemoteDataSource: DescriptionDataSource,
) : DescriptionRepository {
    override suspend fun writeDescription(
        albumId: Int,
        photoId: Int,
        description: String,
    ): Result<Unit> = descriptionRemoteDataSource.createDescription(CreateDescription(albumId, photoId, description))

    override suspend fun editDescription(
        albumId: Int,
        photoId: Int,
        description: String,
    ): Result<Unit> = descriptionRemoteDataSource.editDescription(EditDescription(albumId, photoId, description))

    override suspend fun deleteDescription(
        albumId: Int,
        photoId: Int,
    ): Result<Unit> = descriptionRemoteDataSource.deleteDescription(albumId, photoId)

    override suspend fun getDescription(
        albumId: Int,
        photoId: Int,
    ): Result<DescriptionItem> = descriptionRemoteDataSource.getDescription(albumId, photoId).map { it.mapToDescriptionItem() }
}
