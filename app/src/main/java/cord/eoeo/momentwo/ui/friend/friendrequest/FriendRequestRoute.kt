package cord.eoeo.momentwo.ui.friend.friendrequest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import cord.eoeo.momentwo.ui.model.UserItem
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendRequestRoute(
    coroutineScope: CoroutineScope,
    receivedRequests: () -> List<UserItem>,
    sentRequests: () -> List<UserItem>,
    isReceivedListChanged: () -> Boolean,
    isSentListChanged: () -> Boolean,
    getReceivedRequests: () -> Unit,
    getSentRequests: () -> Unit,
    pagerState: PagerState = rememberPagerState(pageCount = { 2 }),
) {
    FriendRequestScreen(
        coroutineScope = coroutineScope,
        receivedRequests = receivedRequests,
        sentRequests = sentRequests,
        isReceivedListChanged = isReceivedListChanged,
        isSentListChanged = isSentListChanged,
        getReceivedRequests = getReceivedRequests,
        getSentRequests = getSentRequests,
        pagerState = { pagerState },
    )
}
