package cord.eoeo.momentwo.ui.albumdetail.albumsetting

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cord.eoeo.momentwo.ui.model.MemberAuth

@Composable
fun AlbumSettingScreen(
    scrollableState: ScrollableState,
    memberAuth: () -> MemberAuth,
    textColor: () -> Color,
    onOpenTextFieldDialog: (Int) -> Unit,
    onClickChangeImage: () -> Unit,
    onDeleteAlbum: () -> Unit,
    onExitAlbum: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .scrollable(state = scrollableState, orientation = Orientation.Vertical),
    ) {
        if (memberAuth() != MemberAuth.MEMBER) {
            Text(
                text = "관리자 설정",
                fontSize = 13.sp,
                color = textColor(),
                modifier = Modifier.padding(vertical = 8.dp),
            )
            OutlinedCard {
                AlbumSettingListItem(
                    text = { "앨범 제목 수정" },
                    onClick = {
                        onOpenTextFieldDialog(1)
                    },
                )
                AlbumSettingListItem(
                    text = { "앨범 부제목 수정" },
                    onClick = {
                        onOpenTextFieldDialog(2)
                    },
                )
                AlbumSettingListItem(
                    text = { "대표 이미지 수정" },
                    onClick = onClickChangeImage,
                )
                if (memberAuth() == MemberAuth.ADMIN) {
                    // TODO: Dialog 추가
                    AlbumSettingListItem(
                        text = { "앨범 삭제" },
                        onClick = onDeleteAlbum,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "앨범 설정",
            fontSize = 13.sp,
            color = textColor(),
            modifier = Modifier.padding(vertical = 8.dp),
        )
        OutlinedCard {
            // TODO: Dialog 추가
            AlbumSettingListItem(
                text = { "앨범 나가기" },
                onClick = onExitAlbum,
            )
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
