package cord.eoeo.momentwo.feature.albumdetail.impl

import cord.eoeo.momentwo.core.common.*

internal object AlbumDetailReducer : Reducer<AlbumDetailState, AlbumDetailAction> {
    override fun reduce(state: AlbumDetailState, action: AlbumDetailAction): AlbumDetailState = when (action) {
        is SelectionAction -> AlbumSelectionReducer.reduce(state, action)
        is SubAlbumAction -> SubAlbumReducer.reduce(state, action)
        is MemberAction -> AlbumMemberReducer.reduce(state, action)
        is SettingAction -> AlbumSettingReducer.reduce(state, action)
        is AlbumDetailAction.NavigationChanged -> state.copy(selectedNavIndex = action.index, isEditMode = false, editRevision = state.editRevision + 1)
        is AlbumDetailAction.PendingChanged -> state.copy(pendingWrites = if (action.pending) state.pendingWrites + action.key else state.pendingWrites - action.key)
        AlbumDetailAction.FriendsStarted -> state.copy(friends = state.friends.startRefresh())
        is AlbumDetailAction.FriendsLoaded -> state.copy(friends = ScreenState.Success(action.items))
        is AlbumDetailAction.FriendsFailed -> state.copy(friends = state.friends.failRefresh(action.error))
        AlbumDetailAction.FriendsFinished -> state.copy(friends = state.friends.cancelRefresh())
    }
}
