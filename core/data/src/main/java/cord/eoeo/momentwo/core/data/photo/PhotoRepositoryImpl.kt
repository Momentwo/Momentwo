package cord.eoeo.momentwo.core.data.photo

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import cord.eoeo.momentwo.core.data.model.PresignedRequest
import cord.eoeo.momentwo.core.data.model.UploadPhoto
import cord.eoeo.momentwo.core.data.model.UriRequestBody
import cord.eoeo.momentwo.core.data.presigned.PresignedDataSource
import cord.eoeo.momentwo.core.data.photo.PhotoRepository
import cord.eoeo.momentwo.core.model.PhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PhotoRepositoryImpl(
    private val photoRemoteDataSource: PhotoDataSource.Remote,
    private val photoLocalDataSource: PhotoDataSource.Local,
    private val presignedRemoteDataSource: PresignedDataSource,
    private val photoRemoteMediator: PhotoRemoteMediator,
    private val applicationContext: Context,
) : PhotoRepository {
    private val imageLoader = applicationContext.imageLoader
    private val contentResolver = applicationContext.contentResolver

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getPhotoPagingData(
        pageSize: Int,
        albumId: Int,
        subAlbumId: Int,
    ): Flow<PagingData<PhotoItem>> {
        photoRemoteMediator.setParams(pageSize, albumId, subAlbumId)

        return Pager(
            config = PagingConfig(pageSize = pageSize),
            remoteMediator = photoRemoteMediator,
            pagingSourceFactory = { photoLocalDataSource.getPhotoPagingSource(albumId, subAlbumId) },
        ).flow.map { photoPagingData ->
            photoPagingData.map { photoEntity ->
                photoEntity.mapToPhotoItem()
            }
        }
    }

    override suspend fun requestUploadPhoto(
        albumId: Int,
        subAlbumId: Int,
        image: Uri,
    ): Result<Unit> {
        val uriRequestBody = UriRequestBody(contentResolver, image)

        photoRemoteDataSource
            .requestPresignedUrl(
                PresignedRequest(albumId, uriRequestBody.contentType()?.subtype ?: ""),
            ).map { it.presignedUrl }
            .onSuccess { presignedUrl ->
                val filename =
                    presignedUrl
                        .split("?")
                        .first()
                        .split("/")
                        .drop(3)
                        .joinToString("/")

                presignedRemoteDataSource
                    .uploadPhoto(presignedUrl, uriRequestBody)
                    .onSuccess {
                        return photoRemoteDataSource.requestUploadPhoto(
                            UploadPhoto(albumId, subAlbumId, filename),
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
        photoIds: List<Int>,
    ): Result<Unit> {
        val deletePhotosResult =
            photoRemoteDataSource.deletePhotos(albumId, subAlbumId, photoIds.joinToString(","))
        deletePhotosResult.onSuccess {
            photoLocalDataSource.deletePhotos(photoIds)
        }
        return deletePhotosResult
    }

    override suspend fun updateIsLiked(
        photoId: Int,
        isLiked: Boolean,
    ) {
        photoLocalDataSource.updateIsLiked(photoId, isLiked)
    }

    override suspend fun downloadPhoto(imageUrl: String): Result<Unit> {
        val bitmap =
            loadBitmapFromUrl(imageUrl)
                ?: return Result.failure(Exception("Failed to load bitmap"))
        val fileName =
            imageUrl
                .split("/")
                .last()
                .split(".")
                .first()
        val contentValues =
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Momentwo")
            }
        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return Result.failure(Exception("Failed to insert uri"))
        val outputStream =
            contentResolver.openOutputStream(uri)
                ?: return Result.failure(Exception("Failed to insert uri"))

        return photoLocalDataSource.downloadPhoto(bitmap, outputStream)
    }

    private suspend fun loadBitmapFromUrl(imageUrl: String): Bitmap? =
        withContext(Dispatchers.IO) {
            (
                (
                    (
                        imageLoader.execute(
                            ImageRequest
                                .Builder(applicationContext)
                                .data(imageUrl)
                                .allowHardware(false)
                                .build(),
                        ) as? SuccessResult
                    )?.drawable
                ) as? BitmapDrawable
            )?.bitmap
        }
}
