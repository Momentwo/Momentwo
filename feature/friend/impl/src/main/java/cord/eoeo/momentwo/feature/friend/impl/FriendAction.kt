package cord.eoeo.momentwo.feature.friend.impl

import androidx.paging.PagingData
import cord.eoeo.momentwo.core.common.UiError
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow

internal sealed interface FriendAction {
    data class NavigationChanged(val index: Int) : FriendAction
    data class RequestTabChanged(val tab: RequestTab) : FriendAction
    data class DialogChanged(val open: Boolean) : FriendAction
    data object DialogEdited : FriendAction
    data class SearchQueryChanged(val query: String) : FriendAction
    data class SearchReady(val query: String, val source: Flow<PagingData<UserItem>>) : FriendAction
    data class PagingReady(val source: Flow<PagingData<FriendItem>>) : FriendAction
    data class PagingFailed(val error: UiError) : FriendAction
    data class RequestsStarted(val tab: RequestTab) : FriendAction
    data class RequestsLoaded(val tab: RequestTab, val content: FriendRequestsContent, val version: Long) : FriendAction
    data class RequestsFailed(val tab: RequestTab, val error: UiError) : FriendAction
    data class RequestsFinished(val tab: RequestTab) : FriendAction
    data class PendingChanged(val key: FriendWriteKey, val pending: Boolean) : FriendAction
    data class RequestSent(val dialogRevision: Long) : FriendAction
    data class Responded(val userId: Int, val accepted: Boolean) : FriendAction
    data class Cancelled(val userId: Int) : FriendAction
}
