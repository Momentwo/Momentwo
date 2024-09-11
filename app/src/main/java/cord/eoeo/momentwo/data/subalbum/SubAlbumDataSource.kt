package cord.eoeo.momentwo.data.subalbum

import cord.eoeo.momentwo.data.model.CreateSubAlbumInfo
import cord.eoeo.momentwo.data.model.EditSubAlbumInfo
import cord.eoeo.momentwo.data.model.SubAlbumIds
import cord.eoeo.momentwo.data.model.SubAlbumList

interface SubAlbumDataSource {
    suspend fun requestCreateSubAlbum(createSubAlbumInfo: CreateSubAlbumInfo): Result<Unit>

    suspend fun getSubAlbumList(albumId: Int): Result<SubAlbumList>

    suspend fun changeSubAlbumTitle(editSubAlbumInfo: EditSubAlbumInfo): Result<Unit>

    suspend fun deleteSubAlbums(subAlbumIds: SubAlbumIds): Result<Unit>
}
