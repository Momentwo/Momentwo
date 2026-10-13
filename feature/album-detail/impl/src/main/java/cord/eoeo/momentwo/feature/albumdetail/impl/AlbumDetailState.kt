package cord.eoeo.momentwo.feature.albumdetail.impl

import android.net.Uri
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*

internal enum class DetailTab { SubAlbums, Members, Settings }
internal enum class DetailOperation { CreateSubAlbum, DeleteSubAlbums, EditSubAlbumTitle, Invite, Kick, AssignAdmin, Permissions, Exit, DeleteAlbum, Image, Title, Subtitle }
internal data class DetailWriteKey(val operation: DetailOperation, val resourceId: Int? = null) : RequestKey
internal sealed interface AlbumDetailDialog {
    data object None : AlbumDetailDialog
    data object CreateSubAlbum : AlbumDetailDialog
    data object EditTitle : AlbumDetailDialog
    data object EditSubtitle : AlbumDetailDialog
}
internal data class AlbumDetailState(
    val albumItem: AlbumHeader,
    val imageUri: Uri? = null,
    val committedImageUri: Uri? = imageUri,
    val imageRevision: Long = 0,
    val selectedNavIndex: Int = 0,
    val subAlbums: ScreenState<SubAlbumsContent> = ScreenState.Loading,
    val members: ScreenState<MembersContent> = ScreenState.Loading,
    val settings: ScreenState<SettingsContent> = ScreenState.Loading,
    val subAlbumVersion: Long = 0,
    val subAlbumsDirty: Boolean = true,
    val memberVersion: Long = 0,
    val selectedSubAlbumIds: List<Int> = emptyList(),
    val selectedMemberIds: List<Int> = emptyList(),
    val friends: ScreenState<List<FriendItem>> = ScreenState.Loading,
    val selectedFriendList: List<FriendItem> = emptyList(),
    val dialog: AlbumDetailDialog = AlbumDetailDialog.None,
    val dialogRevision: Long = 0,
    val dialogSession: Long = 0,
    val inviteRevision: Long = 0,
    val editRevision: Long = 0,
    val isInviteDialogOpened: Boolean = false,
    val isEditMode: Boolean = false,
    val isInChangeImage: Boolean = false,
    val pendingWrites: Set<DetailWriteKey> = emptySet(),
) : UiState {
    val tab: DetailTab get() = when (selectedNavIndex) { 0 -> DetailTab.SubAlbums; 1 -> DetailTab.Members; else -> DetailTab.Settings }
    val screen: ScreenState<AlbumDetailContent> get() = when (tab) {
        DetailTab.SubAlbums -> subAlbums
        DetailTab.Members -> members
        DetailTab.Settings -> settings
    }
    val permission: MemberAuth? get() = (screen as? ScreenState.Success)?.content?.permission
    fun version(tab: DetailTab): Long = when (tab) { DetailTab.SubAlbums -> subAlbumVersion; DetailTab.Members -> memberVersion; DetailTab.Settings -> 0 }
}
