package cord.eoeo.momentwo.feature.albumdetail.impl

import android.net.Uri
import cord.eoeo.momentwo.core.common.UiError
import cord.eoeo.momentwo.core.model.*

internal sealed interface AlbumDetailAction {
    data class NavigationChanged(val index: Int) : AlbumDetailAction
    data class PendingChanged(val key: DetailWriteKey, val pending: Boolean) : AlbumDetailAction
    data object FriendsStarted : AlbumDetailAction
    data class FriendsLoaded(val items: List<FriendItem>) : AlbumDetailAction
    data class FriendsFailed(val error: UiError) : AlbumDetailAction
    data object FriendsFinished : AlbumDetailAction
}
internal sealed interface SelectionAction : AlbumDetailAction {
    data class SubAlbum(val selected: Boolean, val id: Int) : SelectionAction
    data class Member(val selected: Boolean, val id: Int) : SelectionAction
    data class Friend(val selected: Boolean, val item: FriendItem) : SelectionAction
    data object StartEdit : SelectionAction
    data object CancelEdit : SelectionAction
}
internal sealed interface SubAlbumAction : AlbumDetailAction {
    data object Started : SubAlbumAction
    data class Loaded(val content: SubAlbumsContent, val version: Long) : SubAlbumAction
    data class Failed(val error: UiError) : SubAlbumAction
    data object Finished : SubAlbumAction
    data object Committed : SubAlbumAction
    data object Invalidated : SubAlbumAction
    data class Deleted(val ids: List<Int>, val editRevision: Long) : SubAlbumAction
}
internal sealed interface MemberAction : AlbumDetailAction {
    data object Started : MemberAction
    data class Loaded(val content: MembersContent, val version: Long) : MemberAction
    data class Failed(val error: UiError) : MemberAction
    data object Finished : MemberAction
    data object Committed : MemberAction
    data object OpenInvite : MemberAction
    data object DismissInvite : MemberAction
    data class Invited(val names: Set<String>, val revision: Long) : MemberAction
    data class Kicked(val ids: List<Int>, val editRevision: Long) : MemberAction
}
internal sealed interface SettingAction : AlbumDetailAction {
    data object Started : SettingAction
    data class Loaded(val content: SettingsContent) : SettingAction
    data class Failed(val error: UiError) : SettingAction
    data object Finished : SettingAction
    data class OpenDialog(val dialog: AlbumDetailDialog) : SettingAction
    data object DialogEdited : SettingAction
    data class CloseSubmittedDialog(val revision: Long) : SettingAction
    data class TitleChanged(val title: String, val revision: Long) : SettingAction
    data class SubtitleChanged(val subtitle: String, val revision: Long) : SettingAction
    data class ImageScreenChanged(val open: Boolean) : SettingAction
    data class ImageSelected(val uri: Uri?) : SettingAction
    data object ImageCancelled : SettingAction
    data class ImageSaved(val uri: Uri?, val revision: Long) : SettingAction
}
