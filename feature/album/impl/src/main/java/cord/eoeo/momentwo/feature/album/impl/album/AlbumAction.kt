package cord.eoeo.momentwo.feature.album.impl.album

import cord.eoeo.momentwo.core.common.UiError

internal sealed interface AlbumAction {
    data object Started : AlbumAction
    data class Loaded(val content: AlbumContent, val revision: Long) : AlbumAction
    data class Failed(val error: UiError) : AlbumAction
    data object Finished : AlbumAction
    data object Invalidated : AlbumAction
}
