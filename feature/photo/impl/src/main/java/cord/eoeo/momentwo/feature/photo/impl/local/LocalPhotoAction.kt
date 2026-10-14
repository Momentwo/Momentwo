package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.UiError
import cord.eoeo.momentwo.core.model.LibraryDraft
internal sealed interface LocalPhotoAction {
    data class Snapshot(val content: LocalPhotoContent) : LocalPhotoAction
    data class Failed(val error: UiError) : LocalPhotoAction
    data class Open(val dialog: LocalPhotoDialog, val draft: LibraryDraft) : LocalPhotoAction
    data class Restored(val dialog: LocalPhotoDialog, val draft: LibraryDraft) : LocalPhotoAction
    data object Dismiss : LocalPhotoAction
    data class Edited(val draft: LibraryDraft) : LocalPhotoAction
    data class Saved(val token: String, val interaction: Long) : LocalPhotoAction
    data class Pending(val key: String, val value: Boolean) : LocalPhotoAction
    data object OpenComments:LocalPhotoAction
    data class CommentFailed(val error:UiError):LocalPhotoAction
    data object ToggleImmersive : LocalPhotoAction
}
