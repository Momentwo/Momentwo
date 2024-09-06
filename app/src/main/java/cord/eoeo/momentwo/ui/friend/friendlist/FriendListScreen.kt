package cord.eoeo.momentwo.ui.friend.friendlist

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import cord.eoeo.momentwo.ui.START_EFFECTS_KEY
import cord.eoeo.momentwo.ui.model.FriendItem

@Composable
fun FriendListScreen(
    friendList: () -> List<FriendItem>,
    isFriendListChanged: () -> Boolean,
    getFriendList: () -> Unit,
) {
    LifecycleStartEffect(START_EFFECTS_KEY) {
        getFriendList()
        Log.d("FriendScreen", "FriendListScreen STARTED")
        onStopOrDispose {
            Log.d("FriendScreen", "FriendListScreen STOPED")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = friendList(), key = { it.nickname }) { friendItem ->
            FriendListItem(item = { friendItem })
        }
    }
}
