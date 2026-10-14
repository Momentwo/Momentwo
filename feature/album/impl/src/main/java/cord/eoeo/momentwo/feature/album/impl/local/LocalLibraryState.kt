package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
internal data class LocalLibraryState(val screen: ScreenState<List<LocalAlbum>> = ScreenState.Loading, val creating: Boolean = false, val draft: LibraryDraft = LibraryDraft("", "", ""), val interaction: Long = 0, val pending: Boolean = false) : UiState
