package cord.eoeo.momentwo.feature.auth.impl.login

import cord.eoeo.momentwo.core.common.UiEvent

internal sealed interface LoginEvent : UiEvent {
    data class OnEmailEntered(
        val email: String,
    ) : LoginEvent

    data class OnPasswordEntered(
        val password: String,
    ) : LoginEvent

    data object OnLoginClicked : LoginEvent

    data object OnSignUpClicked : LoginEvent

    data object OnRequestAutoLogin : LoginEvent

    data class OnError(
        val errorMessage: String,
    ) : LoginEvent
}
