package cord.eoeo.momentwo.ui.friend.friendrequest

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cord.eoeo.momentwo.ui.model.FriendRequestItem

@Composable
fun ReceivedFriendRequestItem(
    item: () -> FriendRequestItem,
    onClickAccept: () -> Unit,
    onClickReject: () -> Unit,
) {
    var isAccepted: Boolean by remember { mutableStateOf(false) }

    ReceivedFriendRequestItemScreen(
        item = item,
        isAccepted = { isAccepted },
        onClickAccept = {
            isAccepted = true
            onClickAccept()
        },
        onClickReject = onClickReject,
    )
}

@Composable
fun ReceivedFriendRequestItemScreen(
    item: () -> FriendRequestItem,
    isAccepted: () -> Boolean,
    onClickAccept: () -> Unit,
    onClickReject: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .height(60.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "",
            modifier =
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
        )
        Text(
            text = item().nickname,
            fontSize = 16.sp,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .weight(1f)
                    .padding(start = 8.dp),
        )

        if (item().isUpdated.not()) {
            IconButton(onClick = onClickAccept) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onClickReject) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            Text(
                text = if (isAccepted()) "요청 수락됨" else "요청 거절됨",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(end = 16.dp),
            )
        }
    }
}
