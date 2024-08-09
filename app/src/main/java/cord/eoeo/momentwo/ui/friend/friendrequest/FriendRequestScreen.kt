package cord.eoeo.momentwo.ui.friend.friendrequest

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import cord.eoeo.momentwo.ui.RESUME_EFFECTS_KEY
import cord.eoeo.momentwo.ui.START_EFFECTS_KEY
import cord.eoeo.momentwo.ui.friend.composable.FriendItem
import cord.eoeo.momentwo.ui.friend.composable.FriendRequestItem
import cord.eoeo.momentwo.ui.model.UserItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendRequestScreen(
    coroutineScope: CoroutineScope,
    receivedRequests: () -> List<UserItem>,
    sentRequests: () -> List<UserItem>,
    isReceivedListChanged: () -> Boolean,
    isSentListChanged: () -> Boolean,
    getReceivedRequests: () -> Unit,
    getSentRequests: () -> Unit,
    pagerState: () -> PagerState,
) {
    LifecycleStartEffect(START_EFFECTS_KEY) {
        if (isReceivedListChanged()) {
            getReceivedRequests()
            Log.d("Friend", "ReceivedRequestScreen Started")
        }
        if (isSentListChanged()) {
            getSentRequests()
            Log.d("Friend", "SentRequestScreen Started")
        }
        onStopOrDispose { }
    }

    val tabs = listOf("받은 요청", "보낸 요청")

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TabRow(
            selectedTabIndex = pagerState().currentPage,
            modifier = Modifier.fillMaxWidth(),
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = index == pagerState().currentPage,
                    onClick = { coroutineScope.launch { pagerState().animateScrollToPage(index) } },
                    text = { Text(text = title) },
                )
            }
        }

        HorizontalPager(
            state = pagerState(),
            beyondBoundsPageCount = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) { page ->
            when (page) {
                0 -> {
                    ReceivedRequestScreen(
                        receivedRequests = receivedRequests,
                        isReceivedListChanged = isReceivedListChanged,
                        getReceivedRequests = getReceivedRequests,
                    )
                }

                1 -> {
                    SentRequestScreen(
                        sentRequests = sentRequests,
                        isSentListChanged = isSentListChanged,
                        getSentRequests = getSentRequests,
                    )
                }
            }
        }
    }
}

@Composable
fun ReceivedRequestScreen(
    receivedRequests: () -> List<UserItem>,
    isReceivedListChanged: () -> Boolean,
    getReceivedRequests: () -> Unit,
) {
    LifecycleResumeEffect(RESUME_EFFECTS_KEY) {
        if (isReceivedListChanged()) {
            getReceivedRequests()
            Log.d("Friend", "ReceivedRequestScreen Resumed")
        }
        onPauseOrDispose { }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        /* TODO: 요청 수락, 거절 여부를 알 수 있는 별도의 Data Type 필요 */
        items(items = receivedRequests(), key = { it.id }) { userItem ->
            FriendRequestItem(
                item = { userItem },
                onAcceptRequest = { /*TODO: Accept Request*/ },
                onRejectRequest = { /*TODO: Reject Request*/ },
            )
        }
    }
}

@Composable
fun SentRequestScreen(
    sentRequests: () -> List<UserItem>,
    isSentListChanged: () -> Boolean,
    getSentRequests: () -> Unit,
) {
    LifecycleResumeEffect(RESUME_EFFECTS_KEY) {
        if (isSentListChanged()) {
            getSentRequests()
            Log.d("Friend", "SentRequestScreen Resumed")
        }
        onPauseOrDispose { }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = sentRequests(), key = { it.id }) { userItem ->
            FriendItem(item = { userItem })
        }
    }
}
