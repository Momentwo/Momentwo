package cord.eoeo.momentwo.ui.albumdetail.albumsetting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cord.eoeo.momentwo.ui.model.MemberAuth

@Composable
fun AlbumSettingScreen(
    scrollableState: ScrollableState,
    memberAuth: () -> MemberAuth,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .scrollable(state = scrollableState, orientation = Orientation.Vertical),
    ) {
        AlbumSettingListItem(text = { "앨범 제목 수정" }, onClick = { /* TODO */ })
        AlbumSettingListItem(text = { "대표 이미지 수정" }, onClick = { /* TODO */ })
        if (memberAuth() == MemberAuth.ADMIN) {
            AlbumSettingListItem(text = { "멤버 관리" }, onClick = { /* TODO */ })
        }
    }
}

@Composable
fun AlbumSettingListItem(
    text: () -> String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = text()) },
        trailingContent = { Icon(Icons.AutoMirrored.Default.NavigateNext, text()) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
