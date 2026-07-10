package cord.eoeo.momentwo.core.data.description

import cord.eoeo.momentwo.core.model.DescriptionItem

interface DescriptionRepository {
    suspend fun writeDescription(
        albumId: Int,
        photoId: Int,
        description: String,
    ): Result<Unit>

    suspend fun editDescription(
        albumId: Int,
        photoId: Int,
        description: String,
    ): Result<Unit>

    suspend fun deleteDescription(
        albumId: Int,
        photoId: Int,
    ): Result<Unit>

    suspend fun getDescription(
        albumId: Int,
        photoId: Int,
    ): Result<DescriptionItem>
}
