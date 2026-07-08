package cord.eoeo.momentwo.ui.friend.friendrequest

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import cord.eoeo.momentwo.core.ui.CircleAsyncImage
import cord.eoeo.momentwo.core.model.FriendRequestItem

@Composable
fun SentFriendRequestItem(
    imageLoader: ImageLoader,
    item: () -> FriendRequestItem,
    onClickCancel: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        CircleAsyncImage(
            model = item().userProfileImage,
            contentDescription = "유저 프로필 이미지",
            imageLoader = imageLoader,
            modifier = Modifier
                .padding(4.dp)
                .fillMaxHeight(),
        )

        Text(
            text = item().nickname,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .padding(horizontal = 8.dp)
                .weight(1f),
        )

        if (item().isUpdated.not()) {
            IconButton(
                onClick = onClickCancel,
                modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentHeight(align = Alignment.CenterVertically),
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            Text(
                text = "요청 취소됨",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .padding(end = 16.dp),
            )
        }
    }
}
