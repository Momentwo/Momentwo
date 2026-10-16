package cord.eoeo.momentwo.feature.friend.impl.friendrequest

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import coil3.ImageLoader
import cord.eoeo.momentwo.feature.friend.impl.FriendState
import cord.eoeo.momentwo.feature.friend.impl.FriendEvent
import cord.eoeo.momentwo.feature.friend.impl.RequestTab
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun FriendRequestRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    uiState: () -> FriendState,
    onEvent: (FriendEvent) -> Unit,
    pagerState: PagerState = rememberPagerState(initialPage = if (uiState().requestTab == RequestTab.Received) 0 else 1, pageCount = { 2 }),
) { FriendRequestScreen(coroutineScope, imageLoader, uiState, onEvent, { pagerState }) }
