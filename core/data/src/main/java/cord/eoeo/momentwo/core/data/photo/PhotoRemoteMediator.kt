package cord.eoeo.momentwo.core.data.photo

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import cord.eoeo.momentwo.core.database.PhotoEntity
import cord.eoeo.momentwo.core.database.PhotoRemoteKeyEntity
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class PhotoRemoteMediator(
    private val photoRemoteDataSource: PhotoDataSource.Remote,
    private val photoLocalDataSource: PhotoDataSource.Local,
) : RemoteMediator<Int, PhotoEntity>() {
    private val timeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)
    private var pageSize = 20
    private var albumId = 0
    private var subAlbumId = 0

    fun setParams(pageSize: Int, albumId: Int, subAlbumId: Int) {
        this.pageSize = pageSize
        this.albumId = albumId
        this.subAlbumId = subAlbumId
    }

    override suspend fun initialize(): InitializeAction {
        val lastKey = photoLocalDataSource.getLastKey(albumId, subAlbumId)

        return if (
            lastKey == null ||
            System.currentTimeMillis() - lastKey.lastUpdated > timeout
        ) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PhotoEntity>): MediatorResult {
        val lastKey = when (loadType) {
            LoadType.REFRESH -> {
                Log.d("Photo", "PhotoRemoteMediator LoadType.REFRESH")
                null
            }

            LoadType.PREPEND -> {
                Log.d("Photo", "PhotoRemoteMediator LoadType.PREPEND")
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                Log.d("Photo", "PhotoRemoteMediator LoadType.APPEND")
                val last = photoLocalDataSource.getLastKey(albumId, subAlbumId)
                if (last?.nextCursor == null) return MediatorResult.Success(endOfPaginationReached = true)
                last
            }
        }

        photoRemoteDataSource
            .getPhotoPage(albumId, subAlbumId, pageSize, lastKey?.nextCursor ?: 0)
            .onSuccess { photoPage ->
                if (photoPage.nextCursor == null) {
                    photoLocalDataSource.insertKey(
                        PhotoRemoteKeyEntity(
                            albumId = albumId,
                            subAlbumId = subAlbumId,
                            lastUpdated = System.currentTimeMillis(),
                            nextCursor = null,
                        )
                    )
                    photoLocalDataSource.deleteByRange(
                        emptyList(),
                        lastKey?.nextCursor?.plus(1) ?: 0,
                        photoLocalDataSource.getLastPhotoId() ?: 0
                    )
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                if (loadType == LoadType.REFRESH) {
                    photoLocalDataSource.clearKeys()
                }

                photoRemoteDataSource.getLikedPhoto(
                    subAlbumId,
                    photoPage.images.first().id,
                    photoPage.images.last().id
                ).onSuccess { likedPhotos ->
                    val likedMap = likedPhotos.likesList.associate { it.photoId to true }
                    val photoIds = photoPage.images.map { it.id }

                    photoLocalDataSource.deleteByRange(photoIds, photoIds.first(), photoIds.last())

                    photoLocalDataSource.insertPhotos(
                        photoPage.images.map { photoInfo ->
                            PhotoEntity(
                                id = photoInfo.id,
                                albumId = albumId,
                                subAlbumId = subAlbumId,
                                photoUrl = photoInfo.imageUrl,
                                isLiked = likedMap[photoInfo.id] ?: false,
                            )
                        }
                    )

                    photoLocalDataSource.insertKey(
                        PhotoRemoteKeyEntity(
                            albumId = albumId,
                            subAlbumId = subAlbumId,
                            lastUpdated = System.currentTimeMillis(),
                            nextCursor = photoPage.nextCursor,
                        )
                    )

                    return MediatorResult.Success(endOfPaginationReached = false)
                }.onFailure { exception ->
                    return MediatorResult.Error(exception)
                }
            }.onFailure { exception ->
                return MediatorResult.Error(exception)
            }
        return MediatorResult.Error(Exception("Unknown Error"))
    }
}
