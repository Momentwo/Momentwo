package cord.eoeo.momentwo.data.photo

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import cord.eoeo.momentwo.data.photo.local.entity.PhotoEntity
import cord.eoeo.momentwo.data.photo.local.entity.PhotoRemoteKeyEntity

@OptIn(ExperimentalPagingApi::class)
class PhotoRemoteMediator(
    private val photoRemoteDataSource: PhotoDataSource.Remote,
    private val photoLocalDataSource: PhotoDataSource.Local,
) : RemoteMediator<Int, PhotoEntity>() {
    private var pageSize = 50
    private var albumId = 0
    private var subAlbumId = 0

    fun setParams(pageSize: Int, albumId: Int, subAlbumId: Int) {
        this.pageSize = pageSize
        this.albumId = albumId
        this.subAlbumId = subAlbumId
    }

    override suspend fun initialize(): InitializeAction {
        return if (photoLocalDataSource.getLastKey(albumId, subAlbumId) == null) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PhotoEntity>): MediatorResult {
        val page = when (loadType) {
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
                photoLocalDataSource.getLastKey(albumId, subAlbumId)?.nextPage
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        photoRemoteDataSource
            .getPhotoPage(albumId, subAlbumId, (page ?: 0) * pageSize)
            .onSuccess { photoPage ->
                if (loadType == LoadType.REFRESH) {
                    photoLocalDataSource.clearKeys()
                }

                photoLocalDataSource.insertPhotos(
                    photoPage.images.map { photoInfo ->
                        PhotoEntity(
                            id = photoInfo.id,
                            albumId = albumId,
                            subAlbumId = subAlbumId,
                            imageUrl = photoInfo.imageUrl,
                        )
                    }
                )
                photoLocalDataSource.insertKey(
                    PhotoRemoteKeyEntity(
                        albumId = albumId,
                        subAlbumId = subAlbumId,
                        prevPage = if (photoPage.hasPrevious) photoPage.page - 1 else null,
                        nextPage = if (photoPage.hasNext) photoPage.page + 1 else null,
                    )
                )
                return MediatorResult.Success(endOfPaginationReached = photoPage.hasNext.not())
            }.onFailure { exception ->
                return MediatorResult.Error(exception)
            }
        return MediatorResult.Error(Exception("Unknown Error"))
    }
}
