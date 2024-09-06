package cord.eoeo.momentwo.ui.friend.friendrequest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import cord.eoeo.momentwo.ui.model.FriendRequestItem
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendRequestRoute(
    coroutineScope: CoroutineScope,
    receivedRequests: () -> List<FriendRequestItem>,
    sentRequests: () -> List<FriendRequestItem>,
    isReceivedListChanged: () -> Boolean,
    isSentListChanged: () -> Boolean,
    getReceivedRequests: () -> Unit,
    getSentRequests: () -> Unit,
    onClickAccept: (Int, String) -> Unit,
    onClickReject: (Int, String) -> Unit,
    onClickCancel: (Int, String) -> Unit,
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
        onClickAccept = onClickAccept,
        onClickReject = onClickReject,
        onClickCancel = onClickCancel,
        pagerState = { pagerState },
    )
}
