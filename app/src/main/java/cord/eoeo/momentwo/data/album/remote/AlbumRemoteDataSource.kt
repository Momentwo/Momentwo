package cord.eoeo.momentwo.data.album.remote

import cord.eoeo.momentwo.data.album.AlbumDataSource
import cord.eoeo.momentwo.data.model.AlbumImage
import cord.eoeo.momentwo.data.model.AlbumSubTitle
import cord.eoeo.momentwo.data.model.CreateAlbumInfo
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

    override suspend fun changeAlbumTitle(
        albumId: Int,
        editTitle: String,
    ): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                albumService.putAlbumTitle(albumId, editTitle)
            }
        }
}
