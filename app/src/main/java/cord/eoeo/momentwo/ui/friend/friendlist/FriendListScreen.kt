package cord.eoeo.momentwo.ui.friend.friendlist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil.ImageLoader
import cord.eoeo.momentwo.core.model.FriendItem

@Composable
fun FriendListScreen(
    imageLoader: ImageLoader,
    friendPagingData: () -> LazyPagingItems<FriendItem>,
    isFriendListChanged: () -> Boolean,
) {
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
    }
}
