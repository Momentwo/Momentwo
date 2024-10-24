package cord.eoeo.momentwo.ui.photolist

import android.net.Uri
import androidx.paging.PagingData
import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState
import cord.eoeo.momentwo.ui.model.ImageItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class PhotoListContract {
    data class State(
        val albumId: Int = -1,
        val subAlbumId: Int = -1,
        val albumTitle: String = "",
        val subAlbumTitle: String = "",
        val photoPagingData: Flow<PagingData<ImageItem>> = emptyFlow(),
        val selectedPhotoIds: List<Int> = emptyList(),
        val selectedPhotoUrls: List<String> = emptyList(),
        val isMenuExpended: Boolean = false,
        val isEditMode: Boolean = false,
        val isDialogOpened: Boolean = false,
        val isRefreshing: Boolean = false,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data class OnUploadImage(val imageUri: Uri, val mimeType: String) : Event
        data class OnChangeIsRefreshing(val isRefreshing: Boolean) : Event
        data class OnChangeIsMenuExpended(val isMenuExpended: Boolean) : Event
        data class OnChangeIsEditMode(val isEditMode: Boolean) : Event
        data object OnClickCancelEdit : Event
        data object OnClickConfirmEdit : Event
        data class OnChangeIsSelected(val isSelected: Boolean, val imageItem: ImageItem) : Event
        data class OnChangeIsDialogOpened(val isDialogOpened: Boolean) : Event
        data class OnConfirmDialog(val subAlbumTitle: String) : Event
        data object OnBack : Event
        data class OnError(val errorMessage: String) : Event
    }

    sealed interface Effect : UiEffect {
        data object PopBackStack : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
