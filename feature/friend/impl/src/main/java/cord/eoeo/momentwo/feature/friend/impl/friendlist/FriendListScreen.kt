package cord.eoeo.momentwo.feature.friend.impl.friendlist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil3.ImageLoader
import cord.eoeo.momentwo.core.model.FriendItem
import cord.eoeo.momentwo.core.ui.EmptyContent
import cord.eoeo.momentwo.core.ui.PagingAppendContent
import cord.eoeo.momentwo.core.data.toUiError

@Composable
internal fun FriendListScreen(
    imageLoader: ImageLoader,
    friendPagingData: () -> LazyPagingItems<FriendItem>,
) {
    if (friendPagingData().itemCount == 0) { EmptyContent(); return }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            count = friendPagingData().itemCount,
            key = friendPagingData().itemKey { it.nickname },
        ) { index ->
            friendPagingData()[index]?.let { friendItem ->
                FriendListItem(
                    imageLoader = imageLoader,
                    friendItem = { friendItem },
                )
            }
        }
        item { PagingAppendContent(friendPagingData().loadState.append, Throwable::toUiError, { friendPagingData().retry() }) }
    }
}
