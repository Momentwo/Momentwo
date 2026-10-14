package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
internal data class LocalPhotoListContent(val subAlbum: LocalSubAlbum, val photos: List<LocalPhoto>, val album: LocalAlbum? = null)
internal data class LocalPhotoListState(val id: String, val screen: ScreenState<LocalPhotoListContent> = ScreenState.Loading, val importing: Boolean = false, val editing: Boolean = false, val selected: Set<String> = emptySet(), val menu: Boolean = false, val rename: Boolean = false, val confirmDelete: Boolean = false, val draft: LibraryDraft = LibraryDraft("","",""), val pending: Set<String> = emptySet(), val revision: Long = 0) : UiState
