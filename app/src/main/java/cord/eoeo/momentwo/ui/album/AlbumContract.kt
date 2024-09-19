package cord.eoeo.momentwo.ui.album

import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState
import cord.eoeo.momentwo.ui.model.AlbumItem

class AlbumContract {
    data class State(
        val albumList: List<AlbumItem> = emptyList(),
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data object OnCloseDrawer : Event

        data object OnGetAlbumList : Event

        data class OnError(
            val errorMessage: String,
        ) : Event
    }

    sealed interface Effect : UiEffect {
        data object CloseDrawer : Effect

        data class ShowSnackbar(
            val message: String,
        ) : Effect
    }
}
