package cord.eoeo.momentwo.data.photo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import cord.eoeo.momentwo.data.model.DeletePhotos
import cord.eoeo.momentwo.data.model.PresignedRequest
import cord.eoeo.momentwo.data.model.UploadPhoto
import cord.eoeo.momentwo.data.photo.local.entity.PhotoEntity
import cord.eoeo.momentwo.data.presigned.PresignedDataSource
import cord.eoeo.momentwo.domain.model.UriRequestBody
import cord.eoeo.momentwo.ui.model.ImageItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhotoRepositoryImpl(
    private val photoRemoteDataSource: PhotoDataSource.Remote,
    private val photoLocalDataSource: PhotoDataSource.Local,
    private val presignedRemoteDataSource: PresignedDataSource,
    private val photoRemoteMediator: PhotoRemoteMediator,
    private val applicationContext: Context,
) : PhotoRepository {
    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getPhotoPagingData(pageSize: Int, albumId: Int, subAlbumId: Int): Flow<PagingData<ImageItem>> {
        photoRemoteMediator.setParams(pageSize, albumId, subAlbumId)

        return Pager(
            config = PagingConfig(pageSize = pageSize),
            remoteMediator = photoRemoteMediator,
            pagingSourceFactory = { photoLocalDataSource.getPhotoPagingSource(albumId, subAlbumId) },
        ).flow.map { photoPagingData ->
            photoPagingData.map { photoEntity ->
                photoEntity.mapToImageItem()
            }
        }
    }

    override suspend fun requestUploadPhoto(
        albumId: Int,
        subAlbumId: Int,
        mimeType: String,
        image: Uri
    ): Result<Unit> {
        photoRemoteDataSource.requestPresignedUrl(
            PresignedRequest(albumId, mimeType.split("/").last())
        ).map { it.presignedUrl }
            .onSuccess { presignedUrl ->
                Log.d("Photo", "presignedUrl: $presignedUrl")
                presignedRemoteDataSource.uploadPhoto(
                    presignedUrl,
                    UriRequestBody(applicationContext.contentResolver, image),
                ).onSuccess {
                    return photoRemoteDataSource.requestUploadPhoto(
                        UploadPhoto(
                            albumId,
                            subAlbumId,
                            presignedUrl.split("?").first()
                        )
                    )
                }.onFailure {
                    return Result.failure(Exception("Presigned Upload Failure"))
                }
            }.onFailure {
                return Result.failure(Exception("Presigned URL Failure"))
            }
        return Result.failure(Exception("Upload Photo Failure: Unknown Error"))
    }

    override suspend fun deletePhotos(
        albumId: Int,
        subAlbumId: Int,
        imageIds: List<Int>,
        imageUrls: List<String>
    ): Result<Unit> {
        val deletePhotosResult =
            photoRemoteDataSource.deletePhotos(DeletePhotos(albumId, subAlbumId, imageIds, imageUrls))
        deletePhotosResult.onSuccess {
            photoLocalDataSource.deletePhotos(
                imageIds.mapIndexed { index, photoId ->
                    PhotoEntity(
                        id = photoId,
                        albumId = albumId,
                        subAlbumId = subAlbumId,
                        imageUrl = imageUrls[index]
                    )
                }
            )
        }
        return deletePhotosResult
    }
}
