package cord.eoeo.momentwo.feature.auth.impl.signup

import cord.eoeo.momentwo.core.common.UiEffect

internal sealed interface SignUpEffect : UiEffect {
    data object ScrollToNextPage : SignUpEffect
    data object ScrollToPreviousPage : SignUpEffect
    data object NavigateToLogin : SignUpEffect
    data class ShowSnackbar(val message: String) : SignUpEffect
}
