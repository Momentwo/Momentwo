package cord.eoeo.momentwo.feature.photo.impl.local
import android.net.Uri
import cord.eoeo.momentwo.core.common.UiEvent
internal sealed interface LocalPhotoListEvent : UiEvent {
    data class Menu(val open: Boolean) : LocalPhotoListEvent
    data class Edit(val enabled: Boolean) : LocalPhotoListEvent
    data class Select(val id: String, val checked: Boolean) : LocalPhotoListEvent
    data object Rename : LocalPhotoListEvent
    data object Dismiss : LocalPhotoListEvent
    data class Text(val text: String) : LocalPhotoListEvent
    data object SaveName : LocalPhotoListEvent
    data object Delete : LocalPhotoListEvent
    data object ConfirmDelete : LocalPhotoListEvent
    data object Retry : LocalPhotoListEvent
    data object Back : LocalPhotoListEvent
    data class Open(val id: String) : LocalPhotoListEvent
    data class Import(val sources: List<Uri>) : LocalPhotoListEvent
}
