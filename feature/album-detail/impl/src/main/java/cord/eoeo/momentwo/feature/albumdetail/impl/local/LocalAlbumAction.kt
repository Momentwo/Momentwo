package cord.eoeo.momentwo.feature.albumdetail.impl.local
import cord.eoeo.momentwo.core.common.UiError
import cord.eoeo.momentwo.core.model.LibraryDraft
internal sealed interface LocalAlbumAction {
    data class Snapshot(val content: LocalAlbumContent) : LocalAlbumAction
    data class Failed(val error: UiError) : LocalAlbumAction
    data class Open(val dialog: LocalAlbumDialog, val draft: LibraryDraft) : LocalAlbumAction
    data class Restored(val dialog: LocalAlbumDialog, val draft: LibraryDraft) : LocalAlbumAction
    data object Dismiss : LocalAlbumAction
    data class Edited(val draft: LibraryDraft) : LocalAlbumAction
    data class Saved(val token: String, val interaction: Long) : LocalAlbumAction
    data class Pending(val key: String, val value: Boolean) : LocalAlbumAction
}
