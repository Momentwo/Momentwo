package cord.eoeo.momentwo.feature.album.impl.createalbum

import cord.eoeo.momentwo.core.common.UiEvent
import cord.eoeo.momentwo.core.model.FriendItem

internal sealed interface CreateAlbumEvent : UiEvent {
    data class OnTitleChanged(val title: String) : CreateAlbumEvent

    data object OnClickInviteFriend : CreateAlbumEvent

    data class OnClearFriendItem(
        val nickname: String,
    ) : CreateAlbumEvent

    data class OnCheckedChange(
        val isChecked: Boolean,
        val friendItem: FriendItem,
    ) : CreateAlbumEvent

    data object OnDismissInviteFriend : CreateAlbumEvent

    data object OnConfirmInviteFriend : CreateAlbumEvent

    data object OnGetFriendList : CreateAlbumEvent

    data class OnRequestCreateAlbum(
        val title: String,
    ) : CreateAlbumEvent

    data object OnBack : CreateAlbumEvent

    data class OnError(
        val errorMessage: String,
    ) : CreateAlbumEvent
}
