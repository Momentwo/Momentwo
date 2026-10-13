package cord.eoeo.momentwo.feature.albumdetail.impl

import android.net.Uri
import cord.eoeo.momentwo.core.model.FriendItem
import cord.eoeo.momentwo.core.model.MemberItem
import cord.eoeo.momentwo.core.common.UiEvent

internal sealed interface AlbumDetailEvent : UiEvent {
    data class OnChangeNavIndex(
        val index: Int,
    ) : AlbumDetailEvent

    data object OnGetFriendList : AlbumDetailEvent

    data object OnSubAlbumVisited : AlbumDetailEvent
    data object OnDialogEdited : AlbumDetailEvent
    data class OnRetryTab(val tab: DetailTab) : AlbumDetailEvent
    data object OnGetPermission : AlbumDetailEvent

    data class OnChangeFriendSelected(
        val isSelected: Boolean,
        val friendItem: FriendItem,
    ) : AlbumDetailEvent

    data class OnChangeSubAlbumSelected(
        val isSelected: Boolean,
        val subAlbumId: Int,
    ) : AlbumDetailEvent

    data class OnChangeMemberSelected(
        val isSelected: Boolean,
        val memberId: Int,
    ) : AlbumDetailEvent

    data object OnClickAdd : AlbumDetailEvent

    data object OnClickEdit : AlbumDetailEvent

    data object OnCancelEdit : AlbumDetailEvent

    data object OnConfirmEdit : AlbumDetailEvent

    data class OnOpenTextFieldDialog(
        val dialog: AlbumDetailDialog,
    ) : AlbumDetailEvent

    data object OnDismissTextFieldDialog : AlbumDetailEvent

    data object OnClickInvite : AlbumDetailEvent

    data object OnDismissInviteFriend : AlbumDetailEvent

    data object OnConfirmInviteFriend : AlbumDetailEvent

    data class OnChangeIsInChangeImage(
        val isInChangeImage: Boolean,
    ) : AlbumDetailEvent

    data class OnSelectImage(
        val imageUri: Uri?,
    ) : AlbumDetailEvent

    data object OnCancelChangeImage : AlbumDetailEvent

    data class OnSubAlbumEvents(
        val subAlbumEvents: SubAlbumEvent,
    ) : AlbumDetailEvent

    data class OnMemberEvents(
        val memberEvents: MemberEvent,
    ) : AlbumDetailEvent

    data class OnAlbumSettingEvents(
        val albumSettingEvents: AlbumSettingEvent,
    ) : AlbumDetailEvent

    data object OnBack : AlbumDetailEvent

    data object OnBackInAlbumDetail : AlbumDetailEvent

    data class OnError(
        val errorMessage: String,
    ) : AlbumDetailEvent
}
