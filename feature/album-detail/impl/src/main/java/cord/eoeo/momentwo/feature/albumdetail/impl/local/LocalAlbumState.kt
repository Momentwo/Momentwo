package cord.eoeo.momentwo.feature.albumdetail.impl.local
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
internal data class LocalAlbumContent(val album: LocalAlbum, val subAlbums: List<LocalSubAlbum>)
internal sealed interface LocalAlbumDialog {
    data object None : LocalAlbumDialog
    data object CreateSub : LocalAlbumDialog
    data object EditAlbum : LocalAlbumDialog
    data class RenameSub(val id: String) : LocalAlbumDialog
    data object DeleteAlbum : LocalAlbumDialog
    data class DeleteSub(val id: String) : LocalAlbumDialog
}
internal data class LocalAlbumState(val id: String, val screen: ScreenState<LocalAlbumContent> = ScreenState.Loading, val dialog: LocalAlbumDialog = LocalAlbumDialog.None, val draft: LibraryDraft = LibraryDraft("", "", ""), val interaction: Long = 0, val pending: Set<String> = emptySet()) : UiState
