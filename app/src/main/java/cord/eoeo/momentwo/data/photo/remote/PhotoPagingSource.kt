package cord.eoeo.momentwo.data.photo.remote

import cord.eoeo.momentwo.data.MomentwoPagingSource
import cord.eoeo.momentwo.data.model.PhotoInfo
import cord.eoeo.momentwo.data.photo.PhotoDataSource

class PhotoPagingSource(
    private val photoRemoteDataSource: PhotoDataSource,
    private val pageSize: Int,
    private val albumId: Int,
    private val subAlbumId: Int,
) : MomentwoPagingSource<PhotoInfo>() {
    override suspend fun loadPage(page: Int): LoadResult<Int, PhotoInfo> {
        photoRemoteDataSource
            .getPhotoPage(albumId, subAlbumId, page * pageSize)
            .onSuccess { photoPage ->
                return LoadResult.Page(
                    data = photoPage.images,
                    prevKey = if (photoPage.hasPrevious) page - 1 else null,
                    nextKey = if (photoPage.hasNext) page + 1 else null,
                )
            }.onFailure { exception ->
                return LoadResult.Error(exception)
            }
        return LoadResult.Invalid()
    }
}
