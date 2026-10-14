package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
internal data class LocalPhotoListContent(val subAlbum: LocalSubAlbum, val photos: List<LocalPhoto>)
internal data class LocalPhotoListState(val id: String, val screen: ScreenState<LocalPhotoListContent> = ScreenState.Loading, val importing: Boolean = false) : UiState
