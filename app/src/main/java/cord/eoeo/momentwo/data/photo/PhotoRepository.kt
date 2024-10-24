package cord.eoeo.momentwo.data.photo

import android.net.Uri
import androidx.paging.PagingData
import cord.eoeo.momentwo.ui.model.ImageItem
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    suspend fun getPhotoPagingData(pageSize: Int, albumId: Int, subAlbumId: Int): Flow<PagingData<ImageItem>>

    suspend fun requestUploadPhoto(albumId: Int, subAlbumId: Int, mimeType: String, image: Uri): Result<Unit>

    suspend fun deletePhotos(albumId: Int, subAlbumId: Int, imageIds: List<Int>, imageUrls: List<String>): Result<Unit>
}
