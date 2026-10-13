package cord.eoeo.momentwo.feature.auth.impl.login

import cord.eoeo.momentwo.core.common.UiEffect

internal sealed interface LoginEffect : UiEffect {
    data object NavigateToAlbum : LoginEffect

    data object NavigateToSignUp : LoginEffect

    data class ShowSnackbar(
        val message: String,
    ) : LoginEffect
}
