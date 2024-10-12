package cord.eoeo.momentwo.ui.photolist

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
        val photoList: List<ImageItem> = listOf(
            ImageItem(
                1,
                "https://images.unsplash.com/photo-1724322727370-da4b1fcd8fc0?q=80&w=1587&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            ImageItem(
                2,
                "https://images.unsplash.com/photo-1653999277804-3958bbb35dec?q=80&w=1587&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            ImageItem(
                3,
                "https://images.unsplash.com/photo-1724854102823-13ac1bdb0c3c?q=80&w=1587&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            ImageItem(
                4,
                "https://images.unsplash.com/photo-1725003556921-d4202400abe0?q=80&w=1587&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            ImageItem(
                5,
                "https://images.unsplash.com/photo-1724322725931-439b23158568?q=80&w=1587&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
        ),
        val photoPagingData: Flow<PagingData<ImageItem>> = emptyFlow(),
        val isMenuExpended: Boolean = false,
        val isEditMode: Boolean = false,
        val isDialogOpened: Boolean = false,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data class OnChangeIsMenuExpended(val isMenuExpended: Boolean) : Event
        data class OnChangeIsEditMode(val isEditMode: Boolean) : Event
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
