package cord.eoeo.momentwo.ui.login

import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState

class LoginContract {
    data class State(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data class OnEmailEntered(val email: String) : Event
        data class OnPasswordEntered(val password: String) : Event
        data object OnLoginClicked : Event
        data object OnSignUpClicked : Event
        data object OnRequestAutoLogin : Event
        data class OnError(val errorMessage: String) : Event
    }

    sealed interface Effect : UiEffect {
        data object NavigateToAlbum : Effect
        data object NavigateToSignUp : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
