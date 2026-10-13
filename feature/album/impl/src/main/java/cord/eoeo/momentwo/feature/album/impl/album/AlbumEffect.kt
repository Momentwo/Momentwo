package cord.eoeo.momentwo.feature.album.impl.album

import cord.eoeo.momentwo.core.common.UiEffect

internal sealed interface AlbumEffect : UiEffect {
    data object CloseDrawer : AlbumEffect

    data class ShowSnackbar(
        val message: String,
    ) : AlbumEffect
}
