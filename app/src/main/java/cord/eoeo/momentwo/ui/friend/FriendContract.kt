package cord.eoeo.momentwo.ui.friend

import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState
import cord.eoeo.momentwo.ui.model.UserItem

class FriendContract {
    data class State(
        val selectedNavIndex: Int = 0,
        val friendList: List<UserItem> = emptyList(),
        val receivedRequests: List<UserItem> = emptyList(),
        val sentRequests: List<UserItem> = emptyList(),
        val isFriendListChanged: Boolean = true,
        val isReceivedListChanged: Boolean = true,
        val isSentListChanged: Boolean = true,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data class OnChangeNavIndex(val index: Int) : Event
        data object getFriendList : Event
        data object getReceivedRequests : Event
        data object getSentRequests : Event
        data object OnBack : Event
        data class OnError(val errorMessage: String) : Event
    }

    sealed interface Effect : UiEffect {
        data object PopBackStack : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
