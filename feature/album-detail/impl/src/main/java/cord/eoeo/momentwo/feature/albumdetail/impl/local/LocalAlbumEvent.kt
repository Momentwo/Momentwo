package cord.eoeo.momentwo.feature.albumdetail.impl.local
import android.net.Uri
import cord.eoeo.momentwo.core.common.UiEvent
internal sealed interface LocalAlbumEvent : UiEvent {
    data class Tab(val index: Int) : LocalAlbumEvent
    data object Edit : LocalAlbumEvent
    data object CancelEdit : LocalAlbumEvent
    data class Select(val id: String, val checked: Boolean) : LocalAlbumEvent
    data class SelectMember(val id: Int, val checked: Boolean) : LocalAlbumEvent
    data object Add : LocalAlbumEvent
    data class InviteCheck(val friend: cord.eoeo.momentwo.core.model.FriendItem, val checked: Boolean) : LocalAlbumEvent
    data object ConfirmInvite : LocalAlbumEvent
    data object OpenCover : LocalAlbumEvent
    data object ApplyCover : LocalAlbumEvent
    data object Retry : LocalAlbumEvent
    data class Open(val dialog: LocalAlbumDialog) : LocalAlbumEvent
    data object Dismiss : LocalAlbumEvent
    data class Text(val value: String) : LocalAlbumEvent
    data class Extra(val value: String) : LocalAlbumEvent
    data object Save : LocalAlbumEvent
    data object ConfirmDelete : LocalAlbumEvent
    data class Cover(val uri: Uri?) : LocalAlbumEvent
    data object Back : LocalAlbumEvent
    data object Connect : LocalAlbumEvent
}
