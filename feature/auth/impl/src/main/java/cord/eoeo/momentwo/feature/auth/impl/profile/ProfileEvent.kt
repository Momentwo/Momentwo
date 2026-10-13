package cord.eoeo.momentwo.feature.auth.impl.profile

import cord.eoeo.momentwo.core.common.UiEvent

internal sealed interface ProfileEvent : UiEvent {
    data class OnError(
        val errorMessage: String,
    ) : ProfileEvent
}
