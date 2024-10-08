package cord.eoeo.momentwo.data.album.remote

import cord.eoeo.momentwo.data.album.AlbumDataSource
import cord.eoeo.momentwo.data.model.AlbumImage
import cord.eoeo.momentwo.data.model.AlbumInfoList
import cord.eoeo.momentwo.data.model.AlbumRole
import cord.eoeo.momentwo.data.model.AlbumSubTitle
import cord.eoeo.momentwo.data.model.CreateAlbumInfo
import cord.eoeo.momentwo.data.model.EditAlbumTitle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlbumRemoteDataSource(
    private val albumService: AlbumService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AlbumDataSource {
    override suspend fun requestCreateAlbum(createAlbumInfo: CreateAlbumInfo): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                albumService.postCreateAlbum(createAlbumInfo)
            }
        }

    override suspend fun deleteAlbum(albumId: Int): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                albumService.deleteAlbum(albumId)
            }
        }

    override suspend fun changeAlbumImage(albumImage: AlbumImage): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                albumService.putAlbumImage(albumImage)
            }
        }

    override suspend fun deleteAlbumImage(albumId: Int): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                albumService.deleteAlbumImage(albumId)
            }
        }

    override suspend fun changeAlbumSubTitle(albumSubTitle: AlbumSubTitle): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                albumService.putAlbumSubTitle(albumSubTitle)
            }
        }

    override suspend fun deleteAlbumSubTitle(albumId: Int): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                albumService.deleteAlbumSubTitle(albumId)
            }
        }

    override suspend fun changeAlbumTitle(editTitle: EditAlbumTitle): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                albumService.putAlbumTitle(editTitle)
            }
        }

    override suspend fun getAlbumList(): Result<AlbumInfoList> =
        runCatching {
            withContext(dispatcher) {
                albumService.getAlbumList()
            }
        }

    override suspend fun getAlbumRole(albumId: Int): Result<AlbumRole> =
        runCatching {
            withContext(dispatcher) {
                albumService.getAlbumRole(albumId)
            }
        }
}
