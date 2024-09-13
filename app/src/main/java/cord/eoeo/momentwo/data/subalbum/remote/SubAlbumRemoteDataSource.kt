package cord.eoeo.momentwo.data.subalbum.remote

import cord.eoeo.momentwo.data.model.CreateSubAlbumInfo
import cord.eoeo.momentwo.data.model.EditSubAlbumInfo
import cord.eoeo.momentwo.data.model.SubAlbumIds
import cord.eoeo.momentwo.data.model.SubAlbumList
import cord.eoeo.momentwo.data.subalbum.SubAlbumDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubAlbumRemoteDataSource(
    private val subAlbumService: SubAlbumService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SubAlbumDataSource {
    override suspend fun requestCreateSubAlbum(createSubAlbumInfo: CreateSubAlbumInfo): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                subAlbumService.postCreateSubAlbum(createSubAlbumInfo)
            }
        }

    override suspend fun getSubAlbumList(albumId: Int): Result<SubAlbumList> =
        runCatching {
            withContext(dispatcher) {
                subAlbumService.getSubAlbumList(albumId)
            }
        }

    override suspend fun changeSubAlbumTitle(editSubAlbumInfo: EditSubAlbumInfo): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                subAlbumService.putEditSubAlbum(editSubAlbumInfo)
            }
        }

    override suspend fun deleteSubAlbums(subAlbumIds: SubAlbumIds): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                subAlbumService.deleteSubAlbums(subAlbumIds)
            }
        }
}
