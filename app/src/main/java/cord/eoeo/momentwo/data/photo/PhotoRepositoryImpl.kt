package cord.eoeo.momentwo.data.photo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import cord.eoeo.momentwo.data.photo.remote.PhotoPagingSource
import cord.eoeo.momentwo.ui.model.ImageItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhotoRepositoryImpl(
    private val photoRemoteDataSource: PhotoDataSource,
    private val photoLocalDataSource: PhotoDataSource,
) : PhotoRepository {
    override suspend fun getPhotoPagingData(pageSize: Int, albumId: Int, subAlbumId: Int): Flow<PagingData<ImageItem>> =
        Pager(
            config = PagingConfig(pageSize = pageSize),
            pagingSourceFactory = { PhotoPagingSource(photoRemoteDataSource, pageSize, albumId, subAlbumId) }
        ).flow.map { photoPagingData ->
            photoPagingData.map {
                it.mapToImageItem()
            }
        }
}
