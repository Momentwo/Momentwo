package cord.eoeo.momentwo.feature.friend.impl.friendrequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import coil.ImageLoader
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.core.ui.EmptyContent
import cord.eoeo.momentwo.feature.friend.impl.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun FriendRequestScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    uiState: () -> FriendState,
    onEvent: (FriendEvent) -> Unit,
    pagerState: () -> PagerState,
) {
    LaunchedEffect(pagerState().currentPage) {
        onEvent(FriendEvent.OnSelectRequestTab(if (pagerState().currentPage == 0) RequestTab.Received else RequestTab.Sent))
    }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pagerState().currentPage) {
            listOf("받은 요청", "보낸 요청").forEachIndexed { index, title ->
                Tab(selected = pagerState().currentPage == index,
                    onClick = { coroutineScope.launch { pagerState().animateScrollToPage(index) } }, text = { Text(title) })
            }
        }
        HorizontalPager(pagerState(), modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
            val tab = if (page == 0) RequestTab.Received else RequestTab.Sent
            ScreenContent(uiState().requests(tab), onRetry = {
                onEvent(if (tab == RequestTab.Received) FriendEvent.OnGetReceivedRequests else FriendEvent.OnGetSentRequests)
            }) { content ->
                if (content.requests.isEmpty()) EmptyContent() else LazyColumn(Modifier.fillMaxSize()) {
                    items(content.requests, key = { it.userId }) { item ->
                        when (tab) {
                            RequestTab.Received -> ReceivedFriendRequestItem(
                                imageLoader, { item },
                                onClickAccept = { onEvent(FriendEvent.OnResponseFriendRequest(item.userId, item.nickname, true)) },
                                onClickReject = { onEvent(FriendEvent.OnResponseFriendRequest(item.userId, item.nickname, false)) },
                                isAccepted = { item.userId in uiState().acceptedRequests },
                                isPending = FriendWriteKey.Respond(item.userId) in uiState().pendingWrites,
                            )
                            RequestTab.Sent -> SentFriendRequestItem(imageLoader, { item },
                                onClickCancel = { onEvent(FriendEvent.OnCancelFriendRequest(item.userId)) },
                                isPending = FriendWriteKey.Cancel(item.userId) in uiState().pendingWrites)
                        }
                    }
                }
            }
        }
    }
}
