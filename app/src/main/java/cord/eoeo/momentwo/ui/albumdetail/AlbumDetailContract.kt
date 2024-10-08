package cord.eoeo.momentwo.ui.albumdetail

import android.net.Uri
import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState
import cord.eoeo.momentwo.ui.model.AlbumItem
import cord.eoeo.momentwo.ui.model.FriendItem
import cord.eoeo.momentwo.ui.model.MemberAuth
import cord.eoeo.momentwo.ui.model.MemberItem
import cord.eoeo.momentwo.ui.model.SubAlbumItem

class AlbumDetailContract {
    data class State(
        val albumItem: AlbumItem = AlbumItem(-1, "", "", ""),
        val selectedNavIndex: Int = 0,
        val permission: MemberAuth = MemberAuth.ADMIN,
        val subAlbumList: List<SubAlbumItem> = emptyList(),
        val selectedSubAlbumIds: List<Int> = emptyList(),
        val memberList: List<MemberItem> = emptyList(),
        val selectedMemberNicknames: List<String> = emptyList(),
        val friendList: List<FriendItem> = emptyList(),
        val selectedFriendList: List<FriendItem> = emptyList(),
        val textFieldDialogIndex: Int = -1,
        val isInviteDialogOpened: Boolean = false,
        val isEditMode: Boolean = false,
        val isInChangeImage: Boolean = false,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data class OnChangeNavIndex(val index: Int) : Event
        data object OnGetFriendList : Event
        data object OnGetPermission : Event
        data class OnChangeFriendSelected(val isSelected: Boolean, val friendItem: FriendItem) : Event
        data class OnChangeSubAlbumSelected(val isSelected: Boolean, val subAlbumId: Int) : Event
        data class OnChangeMemberSelected(val isSelected: Boolean, val memberNickname: String) : Event
        data object OnClickAdd : Event
        data object OnClickEdit : Event
        data object OnCancelEdit : Event
        data object OnConfirmEdit : Event
        data class OnOpenTextFieldDialog(val index: Int) : Event
        data object OnDismissTextFieldDialog : Event
        data object OnClickInvite : Event
        data object OnDismissInviteFriend : Event
        data object OnConfirmInviteFriend : Event
        data class OnChangeIsInChangeImage(val isInChangeImage: Boolean) : Event
        data class OnSubAlbumEvents(val subAlbumEvents: SubAlbumEvents) : Event
        data class OnMemberEvents(val memberEvents: MemberEvents) : Event
        data class OnAlbumSettingEvents(val albumSettingEvents: AlbumSettingEvents) : Event
        data object OnBack : Event
        data object OnBackInAlbumDetail : Event
        data class OnError(val errorMessage: String) : Event
    }

    sealed interface Effect : UiEffect {
        data object PopBackStack : Effect
        data object PopBackStackInAlbumDetail : Effect
        data class ShowSnackbar(val message: String) : Effect
    }

    sealed interface SubAlbumEvents {
        data class Create(val subAlbumTitle: String) : SubAlbumEvents
        data object GetSubAlbums : SubAlbumEvents
        data class EditTitle(val subAlbumId: Int, val title: String) : SubAlbumEvents
        data class DeleteSubAlbums(val subAlbumIds: List<Int>) : SubAlbumEvents
    }

    sealed interface MemberEvents {
        data object Exit : MemberEvents
        data object GetMembers : MemberEvents
        data class KickMembers(val kickMembers: List<String>) : MemberEvents
        data class AssignAdmin(val nickname: String) : MemberEvents
        data class EditPermissions(val editMembers: List<MemberItem>) : MemberEvents
    }

    sealed interface AlbumSettingEvents {
        data object DeleteAlbum : AlbumSettingEvents
        data class ChangeImage(val imageUri: Uri) : AlbumSettingEvents
        data object DeleteImage : AlbumSettingEvents
        data class EditSubTitle(val subTitle: String) : AlbumSettingEvents
        data object DeleteSubTitle : AlbumSettingEvents
        data class EditTitle(val title: String) : AlbumSettingEvents
    }
}
