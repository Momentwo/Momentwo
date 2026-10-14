package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.UiEvent
internal sealed interface LibraryCreateEvent : UiEvent {
    data class Text(val text: String) : LibraryCreateEvent
    data object Retry : LibraryCreateEvent
    data object Submit : LibraryCreateEvent
    data object Invite : LibraryCreateEvent
    data object DismissInvite : LibraryCreateEvent
    data class Toggle(val name: String) : LibraryCreateEvent
    data object ConfirmInvite : LibraryCreateEvent
    data class Check(val name: String, val checked: Boolean) : LibraryCreateEvent
    data object Back : LibraryCreateEvent
}
