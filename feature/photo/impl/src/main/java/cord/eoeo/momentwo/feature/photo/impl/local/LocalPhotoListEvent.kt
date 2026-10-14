package cord.eoeo.momentwo.feature.photo.impl.local
import android.net.Uri
import cord.eoeo.momentwo.core.common.UiEvent
internal sealed interface LocalPhotoListEvent : UiEvent {
    data object Retry : LocalPhotoListEvent
    data object Back : LocalPhotoListEvent
    data class Open(val id: String) : LocalPhotoListEvent
    data class Import(val sources: List<Uri>) : LocalPhotoListEvent
}
