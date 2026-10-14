package cord.eoeo.momentwo.feature.photo.impl.photolist

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil.ImageLoader
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.common.RefreshState
import cord.eoeo.momentwo.core.ui.CollectEffect
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.core.ui.EmptyContent
import cord.eoeo.momentwo.core.ui.PagingAppendContent
import cord.eoeo.momentwo.core.data.toUiError
import androidx.compose.runtime.key
import androidx.compose.foundation.lazy.grid.GridItemSpan
import cord.eoeo.momentwo.core.ui.TextFieldDialog
import cord.eoeo.momentwo.core.model.PhotoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoListScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    uiState: () -> PhotoListState,
    effectFlow: () -> Flow<PhotoListEffect>,
    onEvent: (event: PhotoListEvent) -> Unit,
    refreshState: () -> PullToRefreshState,
    photoPagingData: () -> LazyPagingItems<PhotoItem>,
    pagingState: () -> ScreenState<Unit>,
    retrySource: () -> Unit,
    launchGallery: () -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    navigateToPhotoDetail: (Int, Int, String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    CollectEffect(effectFlow()) { effect ->
        when (effect) {
            PhotoListEffect.RefreshPagingData -> photoPagingData().refresh()
            PhotoListEffect.PopBackStack -> popBackStack()
            is PhotoListEffect.ShowSnackbar -> launch { snackbarHostState().showSnackbar(effect.message) }
        }
    }

    if (uiState().isDialogOpened) key(uiState().dialogSession) {
        TextFieldDialog(
            titleText = { "서브 앨범 제목 변경" },
            description = { "변경할 제목을 입력하세요" },
            onDismiss = { onEvent(PhotoListEvent.OnChangeIsDialogOpened(false)) },
            onConfirm = { subAlbumTitle -> onEvent(PhotoListEvent.OnConfirmDialog(subAlbumTitle)) },
            placeholder = { uiState().subAlbumTitle },
            onValueChanged = { onEvent(PhotoListEvent.OnRenameDraftChanged) },
        )
    }

    Scaffold(
        topBar = {
            PhotoListTopAppBar(
                albumTitle = { uiState().albumTitle },
                subAlbumTitle = { uiState().subAlbumTitle },
                isMenuExpended = { uiState().isMenuExpended },
                isEditMode = { uiState().isEditMode },
                onClickBack = { onEvent(PhotoListEvent.OnBack) },
                onClickAdd = launchGallery,
                onClickMenu = { onEvent(PhotoListEvent.OnChangeIsMenuExpended(true)) },
                onDismissMenu = { onEvent(PhotoListEvent.OnChangeIsMenuExpended(false)) },
                onClickEditTitle = {
                    onEvent(PhotoListEvent.OnChangeIsDialogOpened(true))
                    onEvent(PhotoListEvent.OnChangeIsMenuExpended(false))
                },
                onClickEditPhoto = {
                    onEvent(PhotoListEvent.OnChangeIsEditMode(true))
                    onEvent(PhotoListEvent.OnChangeIsMenuExpended(false))
                },
                onClickCancel = { onEvent(PhotoListEvent.OnClickCancelEdit) },
                onClickDelete = { onEvent(PhotoListEvent.OnClickConfirmEdit) },
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValue ->
        PullToRefreshBox(
            isRefreshing = (pagingState() as? ScreenState.Success)?.refresh == RefreshState.Loading,
            state = refreshState(),
            onRefresh = { onEvent(PhotoListEvent.OnRefreshPagingData) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .padding(paddingValue),
        ) {
            ScreenContent(pagingState(), { if (uiState().source is ScreenState.Error) retrySource() else photoPagingData().retry() }) {
                if (photoPagingData().itemCount == 0) EmptyContent() else
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    count = photoPagingData().itemCount,
                    key = photoPagingData().itemKey { it.id },
                ) { index ->
                    photoPagingData()[index]?.let { photoItem ->
                        PhotoListItemBox(
                            imageLoader = imageLoader,
                            photoItem = { photoItem },
                            isEditMode = { uiState().isEditMode },
                            isSelected = { uiState().selectedPhotoIds.contains(photoItem.id) },
                            onChangeSelected = { isSelected ->
                                onEvent(PhotoListEvent.OnChangeIsSelected(isSelected, photoItem.id))
                            },
                            onClick = {
                                navigateToPhotoDetail(
                                    uiState().albumId,
                                    photoItem.id,
                                    photoItem.photoUrl,
                                    photoItem.isLiked,
                                )
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { PagingAppendContent(photoPagingData().loadState.append, Throwable::toUiError, { photoPagingData().retry() }) }
            }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoListTopAppBar(
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
    canAdd: Boolean = true, canRename: Boolean = true, canDelete: Boolean = true,
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
                Icon(Icons.AutoMirrored.Default.ArrowBack, "뒤로")
            }
        },
        actions = {
            if (isEditMode().not()) {
                IconButton(onClick = onClickAdd, enabled = canAdd) {
                    Icon(Icons.Default.Add, "사진 추가")
                }
                IconButton(onClick = onClickMenu) {
                    Icon(Icons.Default.MoreVert, "더보기")
                }

                DropdownMenu(
                    expanded = isMenuExpended(),
                    onDismissRequest = onDismissMenu,
                ) {
                    DropdownMenuItem(
                        text = { Text("서브 앨범 제목 수정") },
                        onClick = onClickEditTitle,
                        enabled = canRename,
                        leadingIcon = { Icon(Icons.Default.Edit, "서브 앨범 제목 수정") },
                    )
                    DropdownMenuItem(
                        text = { Text("사진 삭제") },
                        onClick = onClickEditPhoto,
                        enabled = canDelete,
                        leadingIcon = { Icon(Icons.Default.HideImage, "사진 목록 편집") },
                    )
                }
            } else {
                TextButton(onClick = onClickCancel) {
                    Text(
                        text = "취소",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(onClick = onClickDelete, enabled = canDelete) {
                    Text(
                        text = "삭제",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
    )
}
