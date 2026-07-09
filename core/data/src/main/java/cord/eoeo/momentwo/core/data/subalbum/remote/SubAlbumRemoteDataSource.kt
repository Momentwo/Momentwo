package cord.eoeo.momentwo.core.data.subalbum.remote

import cord.eoeo.momentwo.core.data.model.CreateSubAlbumInfo
import cord.eoeo.momentwo.core.data.model.EditSubAlbumInfo
import cord.eoeo.momentwo.core.data.model.SubAlbumList
import cord.eoeo.momentwo.core.data.subalbum.SubAlbumDataSource
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

    override suspend fun deleteSubAlbums(
        albumId: Int,
        subAlbumIds: String,
    ): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                subAlbumService.deleteSubAlbums(albumId, subAlbumIds)
            }
        }
}
