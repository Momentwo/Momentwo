package cord.eoeo.momentwo.feature.auth.impl.profile

import cord.eoeo.momentwo.core.common.UiEffect

internal sealed interface ProfileEffect : UiEffect {
    data object PopBackStack : ProfileEffect

    data class ShowSnackbar(
        val message: String,
    ) : ProfileEffect
}
