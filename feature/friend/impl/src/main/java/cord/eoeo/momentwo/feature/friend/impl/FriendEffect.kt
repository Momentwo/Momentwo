package cord.eoeo.momentwo.feature.friend.impl

import cord.eoeo.momentwo.core.common.UiEffect

internal sealed interface FriendEffect : UiEffect {
    data object PopBackStack : FriendEffect

    data class ShowSnackbar(
        val message: String,
    ) : FriendEffect
}
