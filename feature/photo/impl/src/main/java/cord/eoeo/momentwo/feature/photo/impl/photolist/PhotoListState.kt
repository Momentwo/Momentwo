package cord.eoeo.momentwo.feature.photo.impl.photolist

import androidx.paging.PagingData
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.PhotoItem
import kotlinx.coroutines.flow.Flow

internal sealed interface PhotoListWriteKey : RequestKey {
    data class Upload(val uri: String) : PhotoListWriteKey
    data object Delete : PhotoListWriteKey
    data object Rename : PhotoListWriteKey
}
internal data class PhotoListState(
    val albumId: Int,
    val subAlbumId: Int,
    val albumTitle: String,
    val subAlbumTitle: String,
    val source: ScreenState<Flow<PagingData<PhotoItem>>> = ScreenState.Loading,
    val selectedPhotoIds: List<Int> = emptyList(),
    val isMenuExpended: Boolean = false,
    val isEditMode: Boolean = false,
    val editSession: Long = 0,
    val isDialogOpened: Boolean = false,
    val dialogRevision: Long = 0,
    val dialogSession: Long = 0,
    val dirty: Boolean = false,
    val pendingWrites: Set<PhotoListWriteKey> = emptySet(),
) : UiState
