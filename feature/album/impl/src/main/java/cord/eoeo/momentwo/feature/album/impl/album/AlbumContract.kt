package cord.eoeo.momentwo.feature.album.impl.album

import cord.eoeo.momentwo.core.common.UiEffect
import cord.eoeo.momentwo.core.common.UiEvent
import cord.eoeo.momentwo.core.common.UiState
import cord.eoeo.momentwo.core.model.AlbumItem

internal class AlbumContract {
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
