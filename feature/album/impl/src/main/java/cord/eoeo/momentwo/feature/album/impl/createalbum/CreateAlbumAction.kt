package cord.eoeo.momentwo.feature.album.impl.createalbum

import cord.eoeo.momentwo.core.common.UiError
import cord.eoeo.momentwo.core.model.FriendItem

internal sealed interface CreateAlbumAction {
    data class TitleChanged(val title: String) : CreateAlbumAction
    data object OpenInvite : CreateAlbumAction
    data object DismissInvite : CreateAlbumAction
    data object ConfirmInvite : CreateAlbumAction
    data class Removed(val nickname: String) : CreateAlbumAction
    data class Checked(val selected: Boolean, val friend: FriendItem) : CreateAlbumAction
    data object FriendsStarted : CreateAlbumAction
    data class FriendsLoaded(val friends: List<FriendItem>) : CreateAlbumAction
    data class FriendsFailed(val error: UiError) : CreateAlbumAction
    data object FriendsFinished : CreateAlbumAction
    data object Started : CreateAlbumAction
    data object Succeeded : CreateAlbumAction
    data class Failed(val error: UiError) : CreateAlbumAction
    data object Finished : CreateAlbumAction
}
