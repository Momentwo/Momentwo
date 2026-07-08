package cord.eoeo.momentwo.data.photo

import android.graphics.Bitmap
import androidx.paging.PagingSource
import cord.eoeo.momentwo.data.model.LikedPhotoList
import cord.eoeo.momentwo.data.model.PhotoPage
import cord.eoeo.momentwo.data.model.PresignedRequest
import cord.eoeo.momentwo.data.model.PresignedUrl
import cord.eoeo.momentwo.data.model.UploadPhoto
import cord.eoeo.momentwo.core.database.PhotoEntity
import cord.eoeo.momentwo.core.database.PhotoRemoteKeyEntity
import java.io.OutputStream

interface PhotoDataSource {
    interface Remote {
        suspend fun getPhotoPage(
            albumId: Int,
            subAlbumId: Int,
            pageSize: Int,
            cursor: Int,
        ): Result<PhotoPage>

        suspend fun requestPresignedUrl(presignedRequest: PresignedRequest): Result<PresignedUrl>

        suspend fun requestUploadPhoto(uploadPhoto: UploadPhoto): Result<Unit>

        suspend fun deletePhotos(
            albumId: Int,
            subAlbumId: Int,
            imageIds: String,
        ): Result<Unit>

        suspend fun getLikedPhoto(
            subAlbumId: Int,
            minPid: Int,
            maxPid: Int,
        ): Result<LikedPhotoList>
    }

    interface Local {
        fun getPhotoPagingSource(
            albumId: Int,
            subAlbumId: Int,
        ): PagingSource<Int, PhotoEntity>

        suspend fun getLastPhotoId(): Int?

        suspend fun insertPhotos(photos: List<PhotoEntity>)

        suspend fun deletePhotos(photoIds: List<Int>)

        suspend fun deleteByRange(
            photoIds: List<Int>,
            start: Int,
            end: Int,
        )

        suspend fun updateIsLiked(
            photoId: Int,
            isLiked: Boolean,
        )

        suspend fun downloadPhoto(
            bitmap: Bitmap,
            outputStream: OutputStream,
        ): Result<Unit>

        suspend fun getLastKey(
            albumId: Int,
            subAlbumId: Int,
        ): PhotoRemoteKeyEntity?

        suspend fun insertKey(key: PhotoRemoteKeyEntity)

        suspend fun clearKeys()
    }
}
