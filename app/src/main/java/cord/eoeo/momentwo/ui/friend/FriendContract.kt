package cord.eoeo.momentwo.ui.friend

import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState
import cord.eoeo.momentwo.ui.model.FriendItem
import cord.eoeo.momentwo.ui.model.FriendRequestItem

class FriendContract {
    data class State(
        val selectedNavIndex: Int = 0,
        val isRequestDialogOpened: Boolean = false,
        val friendList: List<FriendItem> = emptyList(),
        val receivedRequests: List<FriendRequestItem> = emptyList(),
        val sentRequests: List<FriendRequestItem> = emptyList(),
        val isFriendListChanged: Boolean = true,
        val isReceivedListChanged: Boolean = true,
        val isSentListChanged: Boolean = true,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data class OnChangeNavIndex(
            val navIndex: Int,
        ) : Event

        data object OnClickRequestFriend : Event

        data object OnDismissRequestFriend : Event

        data class SendFriendRequest(
            val nickname: String,
        ) : Event

        data class ResponseFriendRequest(
            val itemIndex: Int,
            val nickname: String,
            val isAccepted: Boolean,
        ) : Event

        data class CancelFriendRequest(
            val itemIndex: Int,
            val nickname: String,
        ) : Event

        data object GetFriendList : Event

        data object GetReceivedRequests : Event

        data object GetSentRequests : Event

        data object OnBack : Event

        data class OnError(
            val errorMessage: String,
        ) : Event
    }

    sealed interface Effect : UiEffect {
        data object PopBackStack : Effect

        data class ShowSnackbar(
            val message: String,
        ) : Effect
    }
}
