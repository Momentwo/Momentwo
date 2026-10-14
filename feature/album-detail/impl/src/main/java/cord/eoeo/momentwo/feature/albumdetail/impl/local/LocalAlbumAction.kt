package cord.eoeo.momentwo.feature.albumdetail.impl.local
import cord.eoeo.momentwo.core.common.UiError
import cord.eoeo.momentwo.core.model.LibraryDraft
internal sealed interface LocalAlbumAction {
    data class Tab(val index: Int) : LocalAlbumAction
    data class Editing(val value: Boolean) : LocalAlbumAction
    data class Select(val id: String, val checked: Boolean) : LocalAlbumAction
    data class SelectMember(val id: Int, val checked: Boolean) : LocalAlbumAction
    data class Deleted(val ids: Set<String>, val interaction: Long) : LocalAlbumAction
    data class MembersRemoved(val ids: Set<Int>, val interaction: Long) : LocalAlbumAction
    data class CoverPage(val open: Boolean) : LocalAlbumAction
    data class CoverDraft(val uri: String?) : LocalAlbumAction
    data class Members(val state: cord.eoeo.momentwo.core.common.ScreenState<List<cord.eoeo.momentwo.core.model.MemberItem>>) : LocalAlbumAction
    data class Friends(val state: cord.eoeo.momentwo.core.common.ScreenState<List<cord.eoeo.momentwo.core.model.FriendItem>>) : LocalAlbumAction
    data class InviteCheck(val friend: cord.eoeo.momentwo.core.model.FriendItem, val checked: Boolean) : LocalAlbumAction
    data class Snapshot(val content: LocalAlbumContent) : LocalAlbumAction
    data class Failed(val error: UiError) : LocalAlbumAction
    data class Open(val dialog: LocalAlbumDialog, val draft: LibraryDraft) : LocalAlbumAction
    data class Restored(val dialog: LocalAlbumDialog, val draft: LibraryDraft) : LocalAlbumAction
    data object Dismiss : LocalAlbumAction
    data class Edited(val draft: LibraryDraft) : LocalAlbumAction
    data class Saved(val token: String, val interaction: Long) : LocalAlbumAction
    data class Pending(val key: String, val value: Boolean) : LocalAlbumAction
}
