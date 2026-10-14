package cord.eoeo.momentwo.feature.photo.impl.photodetail.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.key
import cord.eoeo.momentwo.feature.photo.impl.photodetail.PhotoCommentDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil.ImageLoader
import cord.eoeo.momentwo.core.ui.CircleAsyncImage
import cord.eoeo.momentwo.core.ui.TextFieldDialog
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.core.ui.EmptyContent
import cord.eoeo.momentwo.core.ui.PagingAppendContent
import cord.eoeo.momentwo.core.ui.rememberPagingScreenState
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.model.CommentItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CommentBottomSheet(
    imageLoader: ImageLoader,
    commentPagingData: () -> LazyPagingItems<CommentItem>,
    lazyListState: () -> LazyListState,
    onDismiss: () -> Unit,
    onWriteComment: (String) -> Unit,
    onEditComment: (Int, String) -> Unit,
    onDeleteComment: (Int) -> Unit,
    editDialog: () -> PhotoCommentDialog,
    editSession: Long,
    onOpenEditDialog: (Int, String) -> Unit,
    onDismissEditDialog: () -> Unit,
    onEditDraftChanged: () -> Unit,
    draft: () -> String = { "" },
    onDraftChanged: (String) -> Unit = {},
    sourceError: cord.eoeo.momentwo.core.common.UiError? = null,
    onRetrySource: () -> Unit = {},
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lazyColumnNestedScrollConnection = rememberNestedScrollInteropConnection()
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible
    var comment by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    LaunchedEffect(draft()) { if (comment.text != draft()) comment = TextFieldValue(draft()) }
    CommentBottomSheetScreen(
        imageLoader = imageLoader,
        bottomSheetState = { bottomSheetState },
        commentPagingData = commentPagingData,
        lazyListState = lazyListState,
        lazyColumnNestedScrollConnection = { lazyColumnNestedScrollConnection },
        isImeVisible = { isImeVisible },
        isDialogOpened = { editDialog() is PhotoCommentDialog.Edit },
        selectedComment = { (editDialog() as? PhotoCommentDialog.Edit)?.initialText.orEmpty() },
        comment = { comment },
        onDismiss = onDismiss,
        sourceError = sourceError, onRetrySource = onRetrySource,
        onCommentChange = { comment = it; onDraftChanged(it.text) },
        onWriteComment = {
            onWriteComment(comment.text)
        },
        onEditComment = { text -> (editDialog() as? PhotoCommentDialog.Edit)?.let { onEditComment(it.id, text) } },
        onDeleteComment = onDeleteComment,
        onOpenEditDialog = onOpenEditDialog,
        onChangeIsDialogOpened = { if (!it) onDismissEditDialog() },
        editSession = editSession,
        onEditDraftChanged = onEditDraftChanged,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommentBottomSheetScreen(
    imageLoader: ImageLoader,
    bottomSheetState: () -> SheetState,
    commentPagingData: () -> LazyPagingItems<CommentItem>,
    lazyListState: () -> LazyListState,
    lazyColumnNestedScrollConnection: () -> NestedScrollConnection,
    isImeVisible: () -> Boolean,
    isDialogOpened: () -> Boolean,
    selectedComment: () -> String,
    comment: () -> TextFieldValue,
    onDismiss: () -> Unit,
    onCommentChange: (TextFieldValue) -> Unit,
    onWriteComment: () -> Unit,
    onEditComment: (String) -> Unit,
    onDeleteComment: (Int) -> Unit,
    onOpenEditDialog: (Int, String) -> Unit,
    onChangeIsDialogOpened: (Boolean) -> Unit,
    sourceError: cord.eoeo.momentwo.core.common.UiError? = null,
    onRetrySource: () -> Unit = {},
    editSession: Long,
    onEditDraftChanged: () -> Unit,
) {
    LaunchedEffect(lazyListState().canScrollBackward) {
        if (lazyListState().canScrollBackward.not()) {
            lazyListState().stopScroll(MutatePriority.PreventUserInput)
        }
    }

    if (isDialogOpened()) key(editSession) {
        TextFieldDialog(
            titleText = { "댓글 수정" },
            description = { "댓글 내용을 수정하세요" },
            onDismiss = { onChangeIsDialogOpened(false) },
            onConfirm = onEditComment,
            initialText = { selectedComment() },
            onValueChanged = { onEditDraftChanged() },
        )
    }

    cord.eoeo.momentwo.feature.photo.impl.presentation.RecordsSheet("댓글",comment(),onCommentChange,onWriteComment,onDismiss,sheet=bottomSheetState()) {
            val pagingState = rememberPagingScreenState(commentPagingData(), Throwable::toUiError)
            val listState = if (sourceError != null) ScreenState.Error(sourceError) else pagingState
            ScreenContent(listState, { if (sourceError != null) onRetrySource() else commentPagingData().retry() }, Modifier.fillMaxSize()) {
                if (commentPagingData().itemCount == 0) EmptyContent() else
            LazyColumn(
                state = lazyListState(),
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(lazyColumnNestedScrollConnection()),
            ) {
                items(
                    count = commentPagingData().itemCount,
                    key = commentPagingData().itemKey { it.id },
                ) { index ->
                    commentPagingData()[index]?.let { commentItem ->
                        CommentListItem(
                            imageLoader = imageLoader,
                            commentItem = { commentItem },
                            onChangeIsDialogOpened = { open -> if (open) onOpenEditDialog(commentItem.id, commentItem.comment) else onChangeIsDialogOpened(false) },
                            onDeleteComment = { onDeleteComment(commentItem.id) },
                            onLongClick = {},
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                item { PagingAppendContent(commentPagingData().loadState.append, Throwable::toUiError, { commentPagingData().retry() }) }
            }
            }
    }
}

@Composable
internal fun CommentListItem(
    imageLoader: ImageLoader,
    commentItem: () -> CommentItem,
    onChangeIsDialogOpened: (Boolean) -> Unit,
    onDeleteComment: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    CommentListItemScreen(
        imageLoader = imageLoader,
        isMenuExpanded = { isMenuExpanded },
        commentItem = commentItem,
        onDismissMenu = { isMenuExpanded = false },
        onLongClick = {
            onLongClick()
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            isMenuExpanded = true
        },
        onChangeIsDialogOpened = {
            isMenuExpanded = false
            onChangeIsDialogOpened(it)
        },
        onDeleteComment = {
            isMenuExpanded = false
            onDeleteComment()
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CommentListItemScreen(
    imageLoader: ImageLoader,
    isMenuExpanded: () -> Boolean,
    commentItem: () -> CommentItem,
    onDismissMenu: () -> Unit,
    onLongClick: () -> Unit,
    onChangeIsDialogOpened: (Boolean) -> Unit,
    onDeleteComment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    cord.eoeo.momentwo.feature.photo.impl.presentation.RecordRow(imageLoader,commentItem().nickname,commentItem().date,commentItem().userProfileImage,commentItem().comment,
        "댓글",true,{onChangeIsDialogOpened(true)},onDeleteComment,modifier)
}
