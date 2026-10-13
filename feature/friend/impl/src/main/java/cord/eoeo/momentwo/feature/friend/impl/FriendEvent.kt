package cord.eoeo.momentwo.feature.friend.impl

import cord.eoeo.momentwo.core.common.UiEvent

internal sealed interface FriendEvent : UiEvent {
    data class OnChangeNavIndex(
        val navIndex: Int,
    ) : FriendEvent

    data object OnClickRequestFriend : FriendEvent

    data object OnDismissRequestFriend : FriendEvent

    data class OnSendFriendRequest(
        val nickname: String,
    ) : FriendEvent

    data class OnResponseFriendRequest(
        val userId: Int,
        val nickname: String,
        val isAccepted: Boolean,
    ) : FriendEvent

    data class OnCancelFriendRequest(
        val userId: Int,
    ) : FriendEvent

    data class OnSelectRequestTab(val tab: RequestTab) : FriendEvent
    data class OnSearchQueryChanged(val query: String) : FriendEvent
    data object OnRequestDialogEdited : FriendEvent

    data object OnGetReceivedRequests : FriendEvent

    data object OnGetSentRequests : FriendEvent

    data class OnGetSearchUser(
        val query: String,
    ) : FriendEvent

    data object OnBack : FriendEvent

    data class OnError(
        val errorMessage: String,
    ) : FriendEvent
}
