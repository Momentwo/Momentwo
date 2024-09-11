package cord.eoeo.momentwo.data.subalbum.remote

import cord.eoeo.momentwo.data.model.CreateSubAlbumInfo
import cord.eoeo.momentwo.data.model.EditSubAlbumInfo
import cord.eoeo.momentwo.data.model.SubAlbumIds
import cord.eoeo.momentwo.data.model.SubAlbumList
import cord.eoeo.momentwo.data.subalbum.SubAlbumDataSource

class SubAlbumRemoteDataSource(
    private val subAlbumService: SubAlbumService,
) : SubAlbumDataSource {
    override suspend fun requestCreateSubAlbum(createSubAlbumInfo: CreateSubAlbumInfo): Result<Unit> =
        runCatching {
            subAlbumService.postCreateSubAlbum(createSubAlbumInfo)
        }

    override suspend fun getSubAlbumList(albumId: Int): Result<SubAlbumList> =
        runCatching {
            subAlbumService.getSubAlbumList(albumId)
        }

    override suspend fun changeSubAlbumTitle(editSubAlbumInfo: EditSubAlbumInfo): Result<Unit> =
        runCatching {
            subAlbumService.putEditSubAlbum(editSubAlbumInfo)
        }

    override suspend fun deleteSubAlbums(subAlbumIds: SubAlbumIds): Result<Unit> =
        runCatching {
            subAlbumService.deleteSubAlbums(subAlbumIds)
        }
}
