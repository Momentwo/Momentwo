package cord.eoeo.momentwo.ui.profile

import cord.eoeo.momentwo.core.common.UiEffect
import cord.eoeo.momentwo.core.common.UiEvent
import cord.eoeo.momentwo.core.common.UiState

class ProfileContract {
    data class State(
        val nickname: String? = null,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data class OnError(
            val errorMessage: String,
        ) : Event
    }

    sealed interface Effect : UiEffect {
        data object PopBackStack : Effect

        data class ShowSnackbar(
            val message: String,
        ) : Effect
    }
}
