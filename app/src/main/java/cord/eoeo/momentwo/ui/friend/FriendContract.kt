package cord.eoeo.momentwo.ui.friend

import androidx.paging.PagingData
import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState
import cord.eoeo.momentwo.core.model.FriendItem
import cord.eoeo.momentwo.core.model.FriendRequestItem
import cord.eoeo.momentwo.core.model.UserItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FriendContract {
    data class State(
        val selectedNavIndex: Int = 0,
        val isRequestDialogOpened: Boolean = false,
        val friendPagingData: Flow<PagingData<FriendItem>> = emptyFlow(),
        val searchUserPagingData: Flow<PagingData<UserItem>> = emptyFlow(),
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

        data class OnSendFriendRequest(
            val nickname: String,
        ) : Event

        data class OnResponseFriendRequest(
            val itemIndex: Int,
            val nickname: String,
            val isAccepted: Boolean,
        ) : Event

        data class OnCancelFriendRequest(
            val itemIndex: Int,
            val userId: Int,
        ) : Event

        data object OnGetReceivedRequests : Event

        data object OnGetSentRequests : Event

        data class OnGetSearchUser(
            val query: String,
        ) : Event

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
