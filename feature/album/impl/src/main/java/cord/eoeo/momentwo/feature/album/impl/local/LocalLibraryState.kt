package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.LocalAlbum
internal data class LocalLibraryState(val screen: ScreenState<List<LocalAlbum>> = ScreenState.Loading): UiState
