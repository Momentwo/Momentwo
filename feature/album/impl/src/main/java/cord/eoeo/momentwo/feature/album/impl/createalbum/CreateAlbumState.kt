package cord.eoeo.momentwo.feature.album.impl.createalbum

import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.FriendItem

internal sealed interface CreateAlbumStatus {
    data object Idle : CreateAlbumStatus
    data object Submitting : CreateAlbumStatus
    data object Succeeded : CreateAlbumStatus
    data class Failed(val error: UiError) : CreateAlbumStatus
}
internal data class CreateAlbumState(
    val title: String = "",
    val friends: ScreenState<List<FriendItem>> = ScreenState.Loading,
    val isInviteFriendOpened: Boolean = false,
    val tempSelectedFriendList: List<FriendItem> = emptyList(),
    val selectedFriendList: List<FriendItem> = emptyList(),
    val revision: Long = 0,
    val status: CreateAlbumStatus = CreateAlbumStatus.Idle,
) : UiState {
    val isLoading get() = status == CreateAlbumStatus.Submitting
    fun isChecked(friend: FriendItem) = tempSelectedFriendList.any { it.nickname == friend.nickname }
}
