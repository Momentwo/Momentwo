package cord.eoeo.momentwo.feature.album.impl.createalbum

import cord.eoeo.momentwo.core.common.*

internal object CreateAlbumReducer : Reducer<CreateAlbumState, CreateAlbumAction> {
    override fun reduce(state: CreateAlbumState, action: CreateAlbumAction): CreateAlbumState = when (action) {
        is CreateAlbumAction.TitleChanged -> state.copy(title = action.title, revision = state.revision + 1)
        CreateAlbumAction.OpenInvite -> state.copy(tempSelectedFriendList = state.selectedFriendList.toList(), isInviteFriendOpened = true, revision = state.revision + 1)
        CreateAlbumAction.DismissInvite -> state.copy(tempSelectedFriendList = state.selectedFriendList.toList(), isInviteFriendOpened = false, revision = state.revision + 1)
        CreateAlbumAction.ConfirmInvite -> state.copy(selectedFriendList = state.tempSelectedFriendList.toList(), isInviteFriendOpened = false, revision = state.revision + 1)
        is CreateAlbumAction.Removed -> state.copy(selectedFriendList = state.selectedFriendList.filterNot { it.nickname == action.nickname },
            tempSelectedFriendList = state.tempSelectedFriendList.filterNot { it.nickname == action.nickname }, revision = state.revision + 1)
        is CreateAlbumAction.Checked -> state.copy(tempSelectedFriendList = if (action.selected)
            (state.tempSelectedFriendList + action.friend).distinctBy { it.nickname } else state.tempSelectedFriendList.filterNot { it.nickname == action.friend.nickname }, revision = state.revision + 1)
        CreateAlbumAction.FriendsStarted -> state.copy(friends = state.friends.startRefresh())
        is CreateAlbumAction.FriendsLoaded -> {
            val names = action.friends.map { it.nickname }.toSet()
            state.copy(friends = ScreenState.Success(action.friends),
                selectedFriendList = state.selectedFriendList.filter { it.nickname in names },
                tempSelectedFriendList = state.tempSelectedFriendList.filter { it.nickname in names })
        }
        is CreateAlbumAction.FriendsFailed -> state.copy(friends = state.friends.failRefresh(action.error))
        CreateAlbumAction.FriendsFinished -> state.copy(friends = state.friends.cancelRefresh())
        CreateAlbumAction.Started -> state.copy(status = CreateAlbumStatus.Submitting)
        CreateAlbumAction.Succeeded -> state.copy(status = CreateAlbumStatus.Succeeded)
        is CreateAlbumAction.Failed -> state.copy(status = CreateAlbumStatus.Failed(action.error))
        CreateAlbumAction.Finished -> if (state.isLoading) state.copy(status = CreateAlbumStatus.Idle) else state
    }
}
