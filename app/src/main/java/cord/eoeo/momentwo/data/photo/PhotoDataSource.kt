package cord.eoeo.momentwo.data.photo

import cord.eoeo.momentwo.data.model.PhotoPage

interface PhotoDataSource {
    suspend fun getPhotoPage(albumId: Int, subAlbumId: Int, cursor: Int): Result<PhotoPage>
}
