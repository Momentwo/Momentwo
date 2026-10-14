package cord.eoeo.momentwo.feature.albumdetail.impl.albumsetting

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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cord.eoeo.momentwo.feature.albumdetail.impl.AlbumDetailDialog
import cord.eoeo.momentwo.core.model.MemberAuth

@Composable
internal fun AlbumSettingScreen(
    scrollableState: ScrollableState,
    memberAuth: () -> MemberAuth,
    textColor: () -> Color,
    onOpenTextFieldDialog: (AlbumDetailDialog) -> Unit,
    onClickChangeImage: () -> Unit,
    onDeleteAlbum: () -> Unit,
    onExitAlbum: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    var confirm by rememberSaveable { mutableStateOf<String?>(null) }
    if(confirm!=null) AlertDialog(onDismissRequest={confirm=null},title={Text(if(confirm=="delete")"앨범 삭제"else "앨범 나가기")},
        text={Text(if(confirm=="delete")"앨범과 포함된 사진을 삭제할까요?"else "이 앨범에서 나갈까요?")},
        confirmButton={TextButton(onClick={val action=confirm;confirm=null;if(action=="delete")onDeleteAlbum()else onExitAlbum()}){Text("확인")}},
        dismissButton={TextButton(onClick={confirm=null}){Text("취소")}})
    AlbumSettingsContent(scrollableState, memberAuth() != MemberAuth.MEMBER, memberAuth() == MemberAuth.ADMIN, textColor,
        { onOpenTextFieldDialog(AlbumDetailDialog.EditTitle) }, { onOpenTextFieldDialog(AlbumDetailDialog.EditSubtitle) }, onClickChangeImage, { confirm="delete" }, { confirm="leave" })
}

@Composable
internal fun AlbumSettingsContent(scrollableState: ScrollableState, canEdit: Boolean, canDelete: Boolean, textColor: () -> Color,
    editTitle: () -> Unit, editSubtitle: () -> Unit, changeImage: () -> Unit, delete: () -> Unit, leave: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .scrollable(state = scrollableState, orientation = Orientation.Vertical),
    ) {
        if (canEdit) {
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
                        editTitle()
                    },
                )
                AlbumSettingListItem(
                    text = { "앨범 부제목 수정" },
                    onClick = {
                        editSubtitle()
                    },
                )
                AlbumSettingListItem(
                    text = { "대표 이미지 수정" },
                    onClick = changeImage,
                )
                if (canDelete) {
                    // TODO: Dialog 추가
                    AlbumSettingListItem(
                        text = { "앨범 삭제" },
                        onClick = delete,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (leave != null) {
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
                onClick = requireNotNull(leave),
            )
        }
        }
    }
}

@Composable
internal fun AlbumSettingListItem(
    text: () -> String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = text()) },
        trailingContent = { Icon(Icons.AutoMirrored.Default.NavigateNext, text()) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
