package cord.eoeo.momentwo.core.data.subalbum

import cord.eoeo.momentwo.core.data.model.CreateSubAlbumInfo
import cord.eoeo.momentwo.core.data.model.EditSubAlbumInfo
import cord.eoeo.momentwo.core.data.model.SubAlbumList

interface SubAlbumDataSource {
    suspend fun requestCreateSubAlbum(createSubAlbumInfo: CreateSubAlbumInfo): Result<Unit>

    suspend fun getSubAlbumList(albumId: Int): Result<SubAlbumList>

    suspend fun changeSubAlbumTitle(editSubAlbumInfo: EditSubAlbumInfo): Result<Unit>

    suspend fun deleteSubAlbums(
        albumId: Int,
        subAlbumIds: String,
    ): Result<Unit>
}
