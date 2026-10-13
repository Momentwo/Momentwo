package cord.eoeo.momentwo.core.data.album.remote

import cord.eoeo.momentwo.core.common.runSuspendCatching

import cord.eoeo.momentwo.core.data.album.AlbumDataSource
import cord.eoeo.momentwo.core.data.model.AlbumImage
import cord.eoeo.momentwo.core.data.model.AlbumInfoList
import cord.eoeo.momentwo.core.data.model.AlbumRole
import cord.eoeo.momentwo.core.data.model.AlbumSubTitle
import cord.eoeo.momentwo.core.data.model.CreateAlbumInfo
import cord.eoeo.momentwo.core.data.model.EditAlbumTitle
import cord.eoeo.momentwo.core.data.model.PresignedRequest
import cord.eoeo.momentwo.core.data.model.PresignedUrl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AlbumRemoteDataSource(
    private val albumService: AlbumService,
    private val dispatcher: CoroutineDispatcher,
) : AlbumDataSource {
    override suspend fun requestCreateAlbum(createAlbumInfo: CreateAlbumInfo): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                albumService.postCreateAlbum(createAlbumInfo)
            }
        }

    override suspend fun deleteAlbum(albumId: Int): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                albumService.deleteAlbum(albumId)
            }
        }

    override suspend fun requestPresignedUrl(presignedRequest: PresignedRequest): Result<PresignedUrl> =
        runSuspendCatching {
            withContext(dispatcher) {
                albumService.postAlbumPresigned(presignedRequest)
            }
        }

    override suspend fun changeAlbumImage(albumImage: AlbumImage): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                albumService.putAlbumImage(albumImage)
            }
        }

    override suspend fun deleteAlbumImage(albumId: Int): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                albumService.deleteAlbumImage(albumId)
            }
        }

    override suspend fun changeAlbumSubTitle(albumSubTitle: AlbumSubTitle): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                albumService.putAlbumSubTitle(albumSubTitle)
            }
        }

    override suspend fun deleteAlbumSubTitle(albumId: Int): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                albumService.deleteAlbumSubTitle(albumId)
            }
        }

    override suspend fun changeAlbumTitle(editTitle: EditAlbumTitle): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                albumService.putAlbumTitle(editTitle)
            }
        }

    override suspend fun getAlbumList(): Result<AlbumInfoList> =
        runSuspendCatching {
            withContext(dispatcher) {
                albumService.getAlbumList()
            }
        }

    override suspend fun getAlbumRole(albumId: Int): Result<AlbumRole> =
        runSuspendCatching {
            withContext(dispatcher) {
                albumService.getAlbumRole(albumId)
            }
        }
}
