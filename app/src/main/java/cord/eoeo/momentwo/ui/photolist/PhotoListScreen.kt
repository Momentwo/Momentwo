package cord.eoeo.momentwo.ui.photolist

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import cord.eoeo.momentwo.ui.SIDE_EFFECTS_KEY
import cord.eoeo.momentwo.ui.composable.TextFieldDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun PhotoListScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    uiState: () -> PhotoListContract.State,
    effectFlow: () -> Flow<PhotoListContract.Effect>,
    onEvent: (event: PhotoListContract.Event) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        effectFlow()
            .onEach { effect ->
                when (effect) {
                    is PhotoListContract.Effect.PopBackStack -> {
                        popBackStack()
                    }

                    is PhotoListContract.Effect.ShowSnackbar -> {
                        snackbarHostState().showSnackbar(
                            message = effect.message,
                        )
                    }
                }
            }.collect()
    }

    if (uiState().isDialogOpened) {
        TextFieldDialog(
            titleText = { "서브 앨범 제목 변경" },
            description = { "변경할 제목을 입력하세요" },
            onDismiss = { onEvent(PhotoListContract.Event.OnChangeIsDialogOpened(false)) },
            onConfirm = { subAlbumTitle -> onEvent(PhotoListContract.Event.OnConfirmDialog(subAlbumTitle)) },
            placeholder = { uiState().subAlbumTitle }
        )
    }

    Scaffold(
        topBar = {
            PhotoListTopAppBar(
                albumTitle = { uiState().albumTitle },
                subAlbumTitle = { uiState().subAlbumTitle },
                isMenuExpended = { uiState().isMenuExpended },
                isEditMode = { uiState().isEditMode },
                onClickBack = { onEvent(PhotoListContract.Event.OnBack) },
                onClickAdd = { /* TODO: 사진 여러개 선택, 업로드 */ },
                onClickMenu = { onEvent(PhotoListContract.Event.OnChangeIsMenuExpended(true)) },
                onDismissMenu = { onEvent(PhotoListContract.Event.OnChangeIsMenuExpended(false)) },
                onClickEditTitle = {
                    onEvent(PhotoListContract.Event.OnChangeIsDialogOpened(true))
                    onEvent(PhotoListContract.Event.OnChangeIsMenuExpended(false))
                },
                onClickEditPhoto = {
                    onEvent(PhotoListContract.Event.OnChangeIsEditMode(true))
                    onEvent(PhotoListContract.Event.OnChangeIsMenuExpended(false))
                },
                onClickCancel = { onEvent(PhotoListContract.Event.OnChangeIsEditMode(false)) },
                onClickDelete = { /* TODO: 선택한 사진 삭제 */ onEvent(PhotoListContract.Event.OnChangeIsEditMode(false)) },
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValue ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            contentPadding = paddingValue,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        ) {
            items(items = uiState().photoList, key = { it.id }) { imageItem ->
                // TODO: collectAsLazyPagingItems() -> LazyPagingItems -> AsyncImage
                AsyncImage(
                    model = imageItem.imageUrl,
                    contentDescription = "사진",
                    imageLoader = imageLoader,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListTopAppBar(
    albumTitle: () -> String,
    subAlbumTitle: () -> String,
    isMenuExpended: () -> Boolean,
    isEditMode: () -> Boolean,
    onClickBack: () -> Unit,
    onClickAdd: () -> Unit,
    onClickMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onClickEditTitle: () -> Unit,
    onClickEditPhoto: () -> Unit,
    onClickCancel: () -> Unit,
    onClickDelete: () -> Unit,
) {
    TopAppBar(
        title = {
            Row {
                Text(albumTitle())
                Text(
                    text = subAlbumTitle(),
                    color = if (isSystemInDarkTheme()) Color.Gray else Color.DarkGray,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "")
            }
        },
        actions = {
            if (isEditMode().not()) {
                IconButton(onClick = onClickAdd) {
                    Icon(Icons.Default.Add, "")
                }
                IconButton(onClick = onClickMenu) {
                    Icon(Icons.Default.MoreVert, "")
                }

                DropdownMenu(
                    expanded = isMenuExpended(),
                    onDismissRequest = onDismissMenu,
                ) {
                    DropdownMenuItem(
                        text = { Text("서브 앨범 제목 수정") },
                        onClick = onClickEditTitle,
                        leadingIcon = { Icon(Icons.Default.Edit, "서브 앨범 제목 수정") }
                    )
                    DropdownMenuItem(
                        text = { Text("사진 목록 편집") },
                        onClick = onClickEditPhoto,
                        leadingIcon = { Icon(Icons.Default.HideImage, "사진 목록 편집") }
                    )
                }
            } else {
                TextButton(onClick = onClickCancel) {
                    Text(
                        text = "취소",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(onClick = onClickDelete) {
                    Text(
                        text = "삭제",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
    )
}
