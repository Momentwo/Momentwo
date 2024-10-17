package cord.eoeo.momentwo.data.photo

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import cord.eoeo.momentwo.ui.model.ImageItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhotoRepositoryImpl(
    private val photoRemoteDataSource: PhotoDataSource.Remote,
    private val photoLocalDataSource: PhotoDataSource.Local,
    private val photoRemoteMediator: PhotoRemoteMediator,
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
}
