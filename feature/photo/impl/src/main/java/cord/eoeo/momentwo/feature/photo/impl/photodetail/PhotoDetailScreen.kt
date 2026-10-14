package cord.eoeo.momentwo.feature.photo.impl.photodetail

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
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.common.RefreshState
import cord.eoeo.momentwo.core.ui.CollectEffect
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.core.ui.SystemBarsEffect
import kotlinx.coroutines.launch
import cord.eoeo.momentwo.core.model.CommentItem
import cord.eoeo.momentwo.feature.photo.impl.photodetail.composable.CommentBottomSheet
import cord.eoeo.momentwo.feature.photo.impl.photodetail.composable.DescriptionBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

// 몰입형 전체화면 사진 뷰어 — 바(AnimatedVisibility)가 오버레이되며 사진은 edge-to-edge full-bleed 유지
@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun PhotoDetailScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    insetsController: WindowInsetsControllerCompat,
    uiState: () -> PhotoDetailState,
    effectFlow: () -> Flow<PhotoDetailEffect>,
    onEvent: (event: PhotoDetailEvent) -> Unit,
    commentPagingData: () -> LazyPagingItems<CommentItem>,
    lazyListState: () -> LazyListState,
    animatedBackgroundColor: () -> Color,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SystemBarsEffect(insetsController, uiState().barsVisible)
    CollectEffect(effectFlow()) { effect ->
        when (effect) {
            PhotoDetailEffect.RefreshCommentPagingData -> commentPagingData().refresh()
            PhotoDetailEffect.ScrollCommentToBottom -> launch {
                val last = lazyListState().layoutInfo.totalItemsCount - 1
                if (last >= 0) lazyListState().animateScrollToItem(last)
            }
            PhotoDetailEffect.ScrollCommentToTop -> launch { if (lazyListState().layoutInfo.totalItemsCount > 0) lazyListState().scrollToItem(0) }
            PhotoDetailEffect.PopBackStack -> popBackStack()
            is PhotoDetailEffect.ShowSnackbar -> launch { snackbarHostState().showSnackbar(effect.message) }
        }
    }
    val ready = uiState().screen as? ScreenState.Success

    if (uiState().isDescriptionVisible && ready != null) {
        DescriptionBottomSheet(
            imageLoader = imageLoader,
            descriptionItem = { ready.content.description },
            session = uiState().descriptionSession,
            onDraftChanged = { onEvent(PhotoDetailEvent.OnDescriptionDraftChanged) },
            refresh = (uiState().screen as? ScreenState.Success)?.refresh ?: RefreshState.Idle,
            writeCommitted = uiState().confirmedDescriptionRevision != null,
            onRetry = { onEvent(PhotoDetailEvent.OnGetDescription) },
            isWriteDescMode = { uiState().isWriteDescMode },
            onDismiss = {
                onEvent(PhotoDetailEvent.OnChangeIsDescriptionVisible(false))
                onEvent(PhotoDetailEvent.OnChangeIsWriteDescMode(false))
            },
            onChangeIsWriteDescMode = { onEvent(PhotoDetailEvent.OnChangeIsWriteDescMode(it)) },
            onWriteDescription = { title, contents ->
                onEvent(PhotoDetailEvent.OnWriteDescription(title, contents))
            },
            onEditDescription = { title, contents ->
                onEvent(PhotoDetailEvent.OnEditDescription(title, contents))
            },
            onDeleteDescription = { onEvent(PhotoDetailEvent.OnDeleteDescription) },
        )
    }

    if (uiState().isCommentVisible && ready != null) {
        CommentBottomSheet(
            imageLoader = imageLoader,
            commentPagingData = commentPagingData,
            draft = { uiState().commentDraft },
            onDraftChanged = { onEvent(PhotoDetailEvent.OnCommentDraftChanged(it)) },
            editDialog = { uiState().commentDialog },
            editSession = uiState().commentDialogSession,
            onOpenEditDialog = { id, text -> onEvent(PhotoDetailEvent.OnOpenCommentDialog(id, text)) },
            onDismissEditDialog = { onEvent(PhotoDetailEvent.OnDismissCommentDialog) },
            onEditDraftChanged = { onEvent(PhotoDetailEvent.OnCommentEditDraftChanged) },
            sourceError = uiState().commentsError,
            onRetrySource = { onEvent(PhotoDetailEvent.OnChangeIsCommentVisible(true)) },
            lazyListState = lazyListState,
            onDismiss = {
                onEvent(PhotoDetailEvent.OnChangeIsCommentVisible(false))
                onEvent(PhotoDetailEvent.OnScrollCommentToTop)
            },
            onWriteComment = { comment -> onEvent(PhotoDetailEvent.OnWriteComment(comment)) },
            onEditComment = { commentId, comment ->
                onEvent(PhotoDetailEvent.OnEditComment(commentId, comment))
            },
            onDeleteComment = { commentId -> onEvent(PhotoDetailEvent.OnDeleteComment(commentId)) },
        )
    }

    val displayState = if (uiState().isDescriptionVisible && ready != null) ready.copy(refresh = RefreshState.Idle) else uiState().screen
    cord.eoeo.momentwo.feature.photo.impl.presentation.PhotoViewer(imageLoader,uiState().photoUrl,displayState,animatedBackgroundColor(),uiState().barsVisible,uiState().isMenuVisible,snackbarHostState(),
        {onEvent(PhotoDetailEvent.OnBack)},{onEvent(PhotoDetailEvent.OnToggleIsMenuVisible)},{onEvent(PhotoDetailEvent.OnRetry)}) {
        PhotoDetailBottomBar({ready?.content?.likeCount},{ready?.content?.isLiked ?: false},
            {onEvent(PhotoDetailEvent.OnChangeIsDescriptionVisible(true))},{onEvent(PhotoDetailEvent.OnToggleIsLiked)},
            {onEvent(PhotoDetailEvent.OnChangeIsCommentVisible(true))},{onEvent(PhotoDetailEvent.OnDownloadPhoto)})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoDetailTopAppBar(
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = { /* TODO: 사진 날짜 or 찍은 곳 */ },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "뒤로")
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
    )
}

@Composable
internal fun PhotoDetailBottomBar(
    likeCount: () -> Int?,
    isLiked: () -> Boolean,
    onClickDescription: () -> Unit,
    onClickLike: () -> Unit,
    onClickComment: () -> Unit,
    onClickDownload: () -> Unit,
    likeLabel: String = "좋아요", recordLabel: String = "사진 댓글", exportLabel: String = "사진 다운로드",
    canDescribe: Boolean = true, canLike: Boolean = true, canRecord: Boolean = true, canExport: Boolean = true,
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
                    enabled = canDescribe, onClick = onClickDescription,
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
                    enabled = canLike, onClick = onClickLike,
                ),
        ) {
            Icon(if (isLiked()) Icons.Default.Favorite else Icons.Default.FavoriteBorder, likeLabel)
            likeCount()?.let { count -> Text(
                text = count.toString(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            ) } ?: androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
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
                    enabled = canRecord, onClick = onClickComment,
                ),
        ) {
            Icon(Icons.Outlined.ModeComment, recordLabel)
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
                    enabled = canExport, onClick = onClickDownload,
                ),
        ) {
            Icon(Icons.Default.Download, exportLabel)
        }
    }
}
