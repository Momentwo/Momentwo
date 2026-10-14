package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.UiError
internal sealed interface LocalPhotoListAction {
    data class Header(val header: LocalPhotoListHeader) : LocalPhotoListAction
    data object Refreshing : LocalPhotoListAction
    data object Refreshed : LocalPhotoListAction
    data class Menu(val open: Boolean) : LocalPhotoListAction
    data class Edit(val enabled: Boolean) : LocalPhotoListAction
    data class Select(val id: String, val checked: Boolean) : LocalPhotoListAction
    data class Draft(val draft: cord.eoeo.momentwo.core.model.LibraryDraft, val open: Boolean = true) : LocalPhotoListAction
    data object Dismiss : LocalPhotoListAction
    data object ConfirmDelete : LocalPhotoListAction
    data class Deleted(val ids: Set<String>, val revision: Long) : LocalPhotoListAction
    data class Saved(val token: String) : LocalPhotoListAction
    data class Pending(val key: String, val value: Boolean) : LocalPhotoListAction
    data class Snapshot(val content: LocalPhotoListContent) : LocalPhotoListAction
    data class Failed(val error: UiError) : LocalPhotoListAction
    data class Importing(val value: Boolean) : LocalPhotoListAction
}
