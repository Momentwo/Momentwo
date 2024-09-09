package cord.eoeo.momentwo.ui.createalbum

import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState
import cord.eoeo.momentwo.ui.model.FriendItem

class CreateAlbumContract {
    data class State(
        val isInviteFriendOpened: Boolean = false,
        val friendList: List<FriendItem> = emptyList(),
        val tempSelectedFriendList: List<FriendItem> = emptyList(),
        val tempSelectedFriendMap: Map<String, Boolean> = emptyMap(),
        val selectedFriendList: List<FriendItem> = emptyList(),
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data object OnClickInviteFriend : Event

        data class OnClearFriendItem(
            val nickname: String,
        ) : Event

        data class OnCheckedChange(
            val isChecked: Boolean,
            val friendItem: FriendItem,
        ) : Event

        data object OnDismissInviteFriend : Event

        data object OnConfirmInviteFriend : Event

        data object OnGetFriendList : Event

        data class OnRequestCreateAlbum(
            val title: String,
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
