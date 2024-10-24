package cord.eoeo.momentwo.data.photo

import androidx.paging.PagingSource
import cord.eoeo.momentwo.data.model.DeletePhotos
import cord.eoeo.momentwo.data.model.PhotoPage
import cord.eoeo.momentwo.data.model.PresignedRequest
import cord.eoeo.momentwo.data.model.PresignedUrl
import cord.eoeo.momentwo.data.model.UploadPhoto
import cord.eoeo.momentwo.data.photo.local.entity.PhotoEntity
import cord.eoeo.momentwo.data.photo.local.entity.PhotoRemoteKeyEntity

interface PhotoDataSource {
    interface Remote {
        suspend fun getPhotoPage(albumId: Int, subAlbumId: Int, cursor: Int): Result<PhotoPage>

        suspend fun requestPresignedUrl(presignedRequest: PresignedRequest): Result<PresignedUrl>

        suspend fun requestUploadPhoto(uploadPhoto: UploadPhoto): Result<Unit>

        suspend fun deletePhotos(deletePhotos: DeletePhotos): Result<Unit>
    }

    interface Local {
        fun getPhotoPagingSource(albumId: Int, subAlbumId: Int): PagingSource<Int, PhotoEntity>

        suspend fun insertPhotos(photos: List<PhotoEntity>)

        suspend fun deletePhotos(photos: List<PhotoEntity>)

        suspend fun getLastKey(albumId: Int, subAlbumId: Int): PhotoRemoteKeyEntity?

        suspend fun insertKey(key: PhotoRemoteKeyEntity)

        suspend fun clearKeys()
    }
}
