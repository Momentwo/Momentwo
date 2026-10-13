package cord.eoeo.momentwo.feature.friend.impl

import cord.eoeo.momentwo.core.common.*
import kotlinx.coroutines.flow.emptyFlow

internal object FriendReducer : Reducer<FriendState, FriendAction> {
    override fun reduce(state: FriendState, action: FriendAction): FriendState = when (action) {
        is FriendAction.NavigationChanged -> state.copy(selectedNavIndex = action.index)
        is FriendAction.RequestTabChanged -> state.copy(requestTab = action.tab)
        is FriendAction.DialogChanged -> state.copy(isRequestDialogOpened = action.open,
            dialogRevision = state.dialogRevision + 1,
            searchQuery = if (action.open) state.searchQuery else "",
            searchUserPagingData = if (action.open) state.searchUserPagingData else emptyFlow())
        FriendAction.DialogEdited -> state.copy(dialogRevision = state.dialogRevision + 1)
        is FriendAction.SearchQueryChanged -> state.copy(searchQuery = action.query, dialogRevision = state.dialogRevision + 1,
            searchUserPagingData = emptyFlow())
        is FriendAction.SearchReady -> if (state.searchQuery == action.query && state.isRequestDialogOpened)
            state.copy(searchUserPagingData = action.source) else state
        is FriendAction.PagingReady -> state.copy(friendPagingSource = ScreenState.Success(action.source))
        is FriendAction.PagingFailed -> state.copy(friendPagingSource = ScreenState.Error(action.error))
        is FriendAction.RequestsStarted -> state.withRequests(action.tab, state.requests(action.tab).startRefresh())
        is FriendAction.RequestsLoaded -> if (state.version(action.tab) == action.version) {
            val loaded = state.withRequests(action.tab, ScreenState.Success(action.content))
            when (action.tab) {
                RequestTab.Received -> loaded.copy(receivedDirty = false)
                RequestTab.Sent -> loaded.copy(sentDirty = false)
            }
        } else state
        is FriendAction.RequestsFailed -> state.withRequests(action.tab, state.requests(action.tab).failRefresh(action.error))
        is FriendAction.RequestsFinished -> state.withRequests(action.tab, state.requests(action.tab).cancelRefresh())
        is FriendAction.PendingChanged -> state.copy(pendingWrites = if (action.pending)
            state.pendingWrites + action.key else state.pendingWrites - action.key)
        is FriendAction.RequestSent -> state.copy(sentDirty = true, sentVersion = state.sentVersion + 1,
            isRequestDialogOpened = if (state.dialogRevision == action.dialogRevision) false else state.isRequestDialogOpened,
            searchUserPagingData = if (state.dialogRevision == action.dialogRevision) emptyFlow() else state.searchUserPagingData)
        is FriendAction.Responded -> state.copy(
            received = state.received.markUpdated(action.userId), receivedDirty = true,
            receivedVersion = state.receivedVersion + 1,
            friendListRevision = state.friendListRevision + if (action.accepted) 1 else 0,
            acceptedRequests = if (action.accepted) state.acceptedRequests + action.userId else state.acceptedRequests - action.userId,
        )
        is FriendAction.Cancelled -> state.copy(sent = state.sent.markUpdated(action.userId),
            sentDirty = true, sentVersion = state.sentVersion + 1)
    }

    private fun FriendState.withRequests(tab: RequestTab, value: ScreenState<FriendRequestsContent>): FriendState = when (tab) {
        RequestTab.Received -> copy(received = value)
        RequestTab.Sent -> copy(sent = value)
    }
    private fun ScreenState<FriendRequestsContent>.markUpdated(userId: Int): ScreenState<FriendRequestsContent> = when (this) {
        ScreenState.Loading -> this
        is ScreenState.Error -> this
        is ScreenState.Success -> copy(content = content.copy(requests = content.requests.map {
            if (it.userId == userId) it.copy(isUpdated = true) else it
        }))
    }
}
