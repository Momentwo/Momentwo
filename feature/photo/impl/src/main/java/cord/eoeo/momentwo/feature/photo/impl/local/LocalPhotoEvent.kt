package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.UiEvent
internal sealed interface LocalPhotoEvent : UiEvent {
    data class Sheet(val sheet: LocalPhotoSheet) : LocalPhotoEvent
    data class Body(val text: String) : LocalPhotoEvent
    data object DeleteDescription : LocalPhotoEvent
    data object Retry : LocalPhotoEvent
    data object Back : LocalPhotoEvent
    data object ToggleImmersive : LocalPhotoEvent
    data class Open(val dialog: LocalPhotoDialog) : LocalPhotoEvent
    data object Dismiss : LocalPhotoEvent
    data class Text(val value: String) : LocalPhotoEvent
    data object Save : LocalPhotoEvent
    data object OpenComments:LocalPhotoEvent
    data object Refresh:LocalPhotoEvent
    data object Like:LocalPhotoEvent
    data class DeleteComment(val id:String):LocalPhotoEvent
    data object Favorite : LocalPhotoEvent
    data object Export : LocalPhotoEvent
    data object ConfirmDelete : LocalPhotoEvent
    data class DeleteMemo(val id: String) : LocalPhotoEvent
}
