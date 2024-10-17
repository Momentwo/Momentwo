package cord.eoeo.momentwo.data.photo.remote

import cord.eoeo.momentwo.data.model.PhotoPage
import cord.eoeo.momentwo.data.photo.PhotoDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoRemoteDataSource(
    private val photoService: PhotoService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PhotoDataSource.Remote {
    override suspend fun getPhotoPage(albumId: Int, subAlbumId: Int, cursor: Int): Result<PhotoPage> =
        runCatching {
            withContext(dispatcher) {
                photoService.getPhotoPage(albumId, subAlbumId, cursor)
            }
        }
}
