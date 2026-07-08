package cord.eoeo.momentwo.ui.friend.friendrequest

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import coil.ImageLoader
import cord.eoeo.momentwo.core.model.FriendRequestItem
import kotlinx.coroutines.CoroutineScope

@Composable
fun FriendRequestRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    receivedRequests: () -> List<FriendRequestItem>,
    sentRequests: () -> List<FriendRequestItem>,
    isReceivedListChanged: () -> Boolean,
    isSentListChanged: () -> Boolean,
    getReceivedRequests: () -> Unit,
    getSentRequests: () -> Unit,
    onClickAccept: (Int, String) -> Unit,
    onClickReject: (Int, String) -> Unit,
    onClickCancel: (Int, Int) -> Unit,
    pagerState: PagerState = rememberPagerState(pageCount = { 2 }),
) {
    FriendRequestScreen(
        coroutineScope = coroutineScope,
        imageLoader = imageLoader,
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
