package cord.eoeo.momentwo.ui.album

import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState

class AlbumContract {
    data class State(
        val initState: InitState
    ) : UiState

    sealed interface Event : UiEvent {
        data object InitEvent : Event
        data class SecondEvent(val element: Int) : Event
    }

    sealed interface Effect : UiEffect {

    }

    sealed interface InitState {
        data object Init : InitState
    }
}
