package cord.eoeo.momentwo.feature.album.impl.createalbum

import cord.eoeo.momentwo.core.common.UiEffect
import cord.eoeo.momentwo.core.model.FriendItem

internal sealed interface CreateAlbumEffect : UiEffect {
    data object PopBackStack : CreateAlbumEffect

    data class ShowSnackbar(
        val message: String,
    ) : CreateAlbumEffect
}
