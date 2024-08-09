package cord.eoeo.momentwo.ui.friend.friendlist

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import cord.eoeo.momentwo.ui.START_EFFECTS_KEY
import cord.eoeo.momentwo.ui.friend.composable.FriendItem
import cord.eoeo.momentwo.ui.model.UserItem

@Composable
fun FriendListScreen(
    friendList: () -> List<UserItem>,
    isFriendListChanged: () -> Boolean,
    getFriendList: () -> Unit,
) {
    LifecycleStartEffect(START_EFFECTS_KEY) {
        if (isFriendListChanged()) {
            getFriendList()
            Log.d("Friend", "FriendListScreen Started")
        }
        onStopOrDispose { }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = friendList(), key = { it.id }) { userItem ->
            FriendItem(item = { userItem })
        }
    }
}
