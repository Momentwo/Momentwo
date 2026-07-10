package cord.eoeo.momentwo.core.data.description

import cord.eoeo.momentwo.core.data.model.CreateDescription
import cord.eoeo.momentwo.core.data.model.Description
import cord.eoeo.momentwo.core.data.model.EditDescription

interface DescriptionDataSource {
    suspend fun createDescription(createDescription: CreateDescription): Result<Unit>

    suspend fun editDescription(editDescription: EditDescription): Result<Unit>

    suspend fun deleteDescription(
        albumId: Int,
        photoId: Int,
    ): Result<Unit>

    suspend fun getDescription(
        albumId: Int,
        photoId: Int,
    ): Result<Description>
}
