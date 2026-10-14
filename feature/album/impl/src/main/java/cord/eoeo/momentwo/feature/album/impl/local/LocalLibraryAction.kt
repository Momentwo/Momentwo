package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.model.*
internal sealed interface LocalLibraryAction {
    data class Loaded(val albums: List<LocalAlbum>) : LocalLibraryAction
    data object Failed : LocalLibraryAction
    data class Open(val draft: LibraryDraft) : LocalLibraryAction
    data object Dismiss : LocalLibraryAction
    data class Edited(val draft: LibraryDraft) : LocalLibraryAction
    data class Restored(val draft: LibraryDraft) : LocalLibraryAction
    data class Created(val token: String) : LocalLibraryAction
    data class Pending(val value: Boolean) : LocalLibraryAction
}
