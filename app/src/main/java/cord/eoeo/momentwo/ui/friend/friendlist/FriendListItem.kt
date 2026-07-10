package cord.eoeo.momentwo.ui.friend.friendlist

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import cord.eoeo.momentwo.ui.composable.CircleAsyncImage
import cord.eoeo.momentwo.core.model.FriendItem

@Composable
fun FriendListItem(
    imageLoader: ImageLoader,
    friendItem: () -> FriendItem,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        CircleAsyncImage(
            model = friendItem().userProfileImage,
            contentDescription = "프로필 사진",
            imageLoader = imageLoader,
            modifier = Modifier
                .padding(4.dp)
                .height(45.dp),
        )
        Text(
            text = friendItem().nickname,
            fontSize = 16.sp,
            modifier =
            Modifier
                .fillMaxHeight()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .padding(start = 8.dp),
        )
    }
}
