package cord.eoeo.momentwo.data.photo

import androidx.paging.PagingData
import cord.eoeo.momentwo.ui.model.ImageItem
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    suspend fun getPhotoPagingData(pageSize: Int, albumId: Int, subAlbumId: Int): Flow<PagingData<ImageItem>>
}
