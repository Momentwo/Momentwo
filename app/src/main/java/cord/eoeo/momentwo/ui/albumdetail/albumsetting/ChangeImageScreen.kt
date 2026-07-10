package cord.eoeo.momentwo.ui.albumdetail.albumsetting

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import cord.eoeo.momentwo.core.common.SIDE_EFFECTS_KEY
import cord.eoeo.momentwo.ui.composable.AlbumItemCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeImageScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    imageUri: () -> Uri?,
    title: () -> String,
    subTitle: () -> String,
    isEdit: () -> Boolean,
    bottomSheetState: () -> SheetState,
    onChangeIsInChangeImage: (Boolean) -> Unit,
    onChangeIsEdit: (Boolean) -> Unit,
    onClickSelect: () -> Unit,
    onClickReset: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        onChangeIsInChangeImage(true)
    }

    BackHandler(onBack = onBack)

    if (isEdit()) {
        ModalBottomSheet(
            onDismissRequest = { onChangeIsEdit(false) },
            sheetState = bottomSheetState(),
        ) {
            ListItem(
                leadingContent = { Icon(Icons.Default.PhotoAlbum, "앨범에서 이미지 선택") },
                headlineContent = { Text("앨범에서 이미지 선택") },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.clickable {
                    coroutineScope.launch { bottomSheetState().hide() }.invokeOnCompletion {
                        if (bottomSheetState().isVisible.not()) {
                            onClickSelect()
                            onChangeIsEdit(false)
                        }
                    }
                },
            )

            ListItem(
                leadingContent = { Icon(Icons.Default.Clear, "기본 이미지로 변경") },
                headlineContent = { Text("기본 이미지로 변경") },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.clickable {
                    coroutineScope.launch { bottomSheetState().hide() }.invokeOnCompletion {
                        if (bottomSheetState().isVisible.not()) {
                            onClickReset()
                            onChangeIsEdit(false)
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Box(
            contentAlignment = Alignment.TopEnd,
        ) {
            AlbumItemCard(
                imageLoader = imageLoader,
                image = imageUri,
                title = title,
                subTitle = subTitle,
                onClick = { onChangeIsEdit(true) },
            )

            SmallFloatingActionButton(
                onClick = { onChangeIsEdit(true) },
                modifier = Modifier.padding(28.dp),
            ) {
                Icon(Icons.Default.Edit, "앨범 이미지 수정")
            }
        }
    }
}
