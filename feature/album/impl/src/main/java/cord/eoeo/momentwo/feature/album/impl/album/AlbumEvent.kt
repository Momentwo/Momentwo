package cord.eoeo.momentwo.feature.album.impl.album

import cord.eoeo.momentwo.core.common.UiEvent

internal sealed interface AlbumEvent : UiEvent {
    data object OnCloseDrawer : AlbumEvent

    data object OnInvalidate : AlbumEvent

    data object OnGetAlbumList : AlbumEvent

    data class OnError(
        val errorMessage: String,
    ) : AlbumEvent
}
