package cord.eoeo.momentwo.ui.photodetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.paging.compose.LazyPagingItems
import coil.ImageLoader
import coil.compose.AsyncImage
import cord.eoeo.momentwo.ui.SIDE_EFFECTS_KEY
import cord.eoeo.momentwo.core.model.CommentItem
import cord.eoeo.momentwo.ui.photodetail.composable.CommentBottomSheet
import cord.eoeo.momentwo.ui.photodetail.composable.DescriptionBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun PhotoDetailScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    insetsController: WindowInsetsControllerCompat,
    uiState: () -> PhotoDetailContract.State,
    effectFlow: () -> Flow<PhotoDetailContract.Effect>,
    onEvent: (event: PhotoDetailContract.Event) -> Unit,
    commentPagingData: () -> LazyPagingItems<CommentItem>,
    lazyListState: () -> LazyListState,
    animatedBackgroundColor: () -> Color,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        onEvent(PhotoDetailContract.Event.OnGetLikeCount)
        onEvent(PhotoDetailContract.Event.OnGetDescription)

        effectFlow()
            .onEach { effect ->
                when (effect) {
                    is PhotoDetailContract.Effect.RefreshCommentPagingData -> {
                        commentPagingData().refresh()
                    }

                    is PhotoDetailContract.Effect.ScrollCommentToBottom -> {
                        lazyListState().animateScrollToItem(lazyListState().layoutInfo.totalItemsCount)
                    }

                    is PhotoDetailContract.Effect.ScrollCommentToTop -> {
                        lazyListState().scrollToItem(0)
                    }

                    is PhotoDetailContract.Effect.ToggleSystemBars -> {
                        if (effect.isVisible) {
                            insetsController.apply {
                                show(WindowInsetsCompat.Type.statusBars())
                                show(WindowInsetsCompat.Type.navigationBars())
                                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                            }
                        } else {
                            insetsController.apply {
                                hide(WindowInsetsCompat.Type.statusBars())
                                hide(WindowInsetsCompat.Type.navigationBars())
                                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            }
                        }
                    }

                    is PhotoDetailContract.Effect.PopBackStack -> {
                        popBackStack()
                    }

                    is PhotoDetailContract.Effect.ShowSnackbar -> {
                        snackbarHostState().showSnackbar(
                            message = effect.message,
                        )
                    }
                }
            }.collect()
    }

    if (uiState().isDescriptionVisible) {
        DescriptionBottomSheet(
            imageLoader = imageLoader,
            descriptionItem = { uiState().descriptionItem },
            isWriteDescMode = { uiState().isWriteDescMode },
            onDismiss = {
                onEvent(PhotoDetailContract.Event.OnChangeIsDescriptionVisible(false))
                onEvent(PhotoDetailContract.Event.OnChangeIsWriteDescMode(false))
            },
            onChangeIsWriteDescMode = { onEvent(PhotoDetailContract.Event.OnChangeIsWriteDescMode(it)) },
            onWriteDescription = { title, contents ->
                onEvent(PhotoDetailContract.Event.OnWriteDescription(title, contents))
                onEvent(PhotoDetailContract.Event.OnChangeIsWriteDescMode(false))
            },
            onEditDescription = { title, contents ->
                onEvent(PhotoDetailContract.Event.OnEditDescription(title, contents))
                onEvent(PhotoDetailContract.Event.OnChangeIsWriteDescMode(false))
            },
            onDeleteDescription = { onEvent(PhotoDetailContract.Event.OnDeleteDescription) },
        )
    }

    if (uiState().isCommentVisible) {
        CommentBottomSheet(
            imageLoader = imageLoader,
            commentPagingData = commentPagingData,
            lazyListState = lazyListState,
            onDismiss = {
                onEvent(PhotoDetailContract.Event.OnChangeIsCommentVisible(false))
                onEvent(PhotoDetailContract.Event.OnScrollCommentToTop)
            },
            onWriteComment = { comment -> onEvent(PhotoDetailContract.Event.OnWriteComment(comment)) },
            onEditComment = { commentId, comment ->
                onEvent(PhotoDetailContract.Event.OnEditComment(commentId, comment))
            },
            onDeleteComment = { commentId -> onEvent(PhotoDetailContract.Event.OnDeleteComment(commentId)) },
        )
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = uiState().isMenuVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                PhotoDetailTopAppBar(
                    onBack = { onEvent(PhotoDetailContract.Event.OnBack) }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = uiState().isMenuVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                PhotoDetailBottomBar(
                    likeCount = { uiState().likeCount },
                    isLiked = { uiState().isLiked },
                    onClickDescription = { onEvent(PhotoDetailContract.Event.OnChangeIsDescriptionVisible(true)) },
                    onClickLike = { onEvent(PhotoDetailContract.Event.OnToggleIsLiked) },
                    onClickComment = { onEvent(PhotoDetailContract.Event.OnChangeIsCommentVisible(true)) },
                    onClickDownload = { onEvent(PhotoDetailContract.Event.OnDownloadPhoto) },
                )
            }
        },
        snackbarHost = { SnackbarHost(SnackbarHostState()) },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        AsyncImage(
            model = uiState().photoUrl,
            contentDescription = "사진",
            imageLoader = imageLoader,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawRect(animatedBackgroundColor()) }
                .clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = { onEvent(PhotoDetailContract.Event.OnToggleIsMenuVisible) }
                ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailTopAppBar(
    onBack: () -> Unit,
) {
    TopAppBar(
        title = { /* TODO: 사진 날짜 or 찍은 곳 */ },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
    )
}

@Composable
fun PhotoDetailBottomBar(
    likeCount: () -> Int,
    isLiked: () -> Boolean,
    onClickDescription: () -> Unit,
    onClickLike: () -> Unit,
    onClickComment: () -> Unit,
    onClickDownload: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Max)
            .background(color = Color.Transparent)
            .navigationBarsPadding()
            .padding(8.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxHeight()
                .weight(1f)
                .clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = onClickDescription,
                ),
        ) {
            Icon(Icons.AutoMirrored.Outlined.Article, "사진 설명")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxHeight()
                .weight(1f)
                .clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = onClickLike,
                ),
        ) {
            Icon(if (isLiked()) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "좋아요")
            Text(
                text = likeCount().toString(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxHeight()
                .weight(1f)
                .clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = onClickComment,
                ),
        ) {
            Icon(Icons.Outlined.ModeComment, "사진 댓글")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxHeight()
                .weight(1f)
                .clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = onClickDownload,
                ),
        ) {
            Icon(Icons.Default.Download, "사진 다운로드")
        }
    }
}
