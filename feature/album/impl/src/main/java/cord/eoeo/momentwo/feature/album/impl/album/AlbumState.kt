package cord.eoeo.momentwo.feature.album.impl.album

import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.AlbumItem

internal data class AlbumContent(val albums: List<AlbumItem>)
internal data class AlbumState(
    val screen: ScreenState<AlbumContent> = ScreenState.Loading,
    val revision: Long = 0,
    val dirty: Boolean = true,
) : UiState
