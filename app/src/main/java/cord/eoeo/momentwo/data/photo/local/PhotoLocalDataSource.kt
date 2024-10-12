package cord.eoeo.momentwo.data.photo.local

import cord.eoeo.momentwo.data.model.PhotoPage
import cord.eoeo.momentwo.data.photo.PhotoDataSource

class PhotoLocalDataSource(
    private val photoDao: PhotoDao,
) : PhotoDataSource {
    override suspend fun getPhotoPage(albumId: Int, subAlbumId: Int, cursor: Int): Result<PhotoPage> {
        TODO("Not yet implemented")
    }

}
