package cord.eoeo.momentwo.ui.photodetail.composable

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
import cord.eoeo.momentwo.core.model.CommentItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommentBottomSheet(
    imageLoader: ImageLoader,
    commentPagingData: () -> LazyPagingItems<CommentItem>,
    lazyListState: () -> LazyListState,
    onDismiss: () -> Unit,
    onWriteComment: (String) -> Unit,
    onEditComment: (Int, String) -> Unit,
    onDeleteComment: (Int) -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lazyColumnNestedScrollConnection = rememberNestedScrollInteropConnection()
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible
    var comment by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var isDialogOpened by rememberSaveable { mutableStateOf(false) }
    var selectedCommentId by rememberSaveable { mutableIntStateOf(-1) }
    var selectedComment by rememberSaveable { mutableStateOf("") }

    CommentBottomSheetScreen(
        imageLoader = imageLoader,
        bottomSheetState = { bottomSheetState },
        commentPagingData = commentPagingData,
        lazyListState = lazyListState,
        lazyColumnNestedScrollConnection = { lazyColumnNestedScrollConnection },
        isImeVisible = { isImeVisible },
        isDialogOpened = { isDialogOpened },
        selectedComment = { selectedComment },
        comment = { comment },
        onDismiss = onDismiss,
        onCommentChange = { comment = it },
        onWriteComment = {
            onWriteComment(comment.text)
            comment = TextFieldValue("")
            focusManager.clearFocus(force = true)
        },
        onEditComment = {
            onEditComment(selectedCommentId, it)
            isDialogOpened = false
        },
        onDeleteComment = { onDeleteComment(selectedCommentId) },
        onExpandMenu = { selectedId, selectedText ->
            selectedCommentId = selectedId
            selectedComment = selectedText
        },
        onChangeIsDialogOpened = { isDialogOpened = it },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheetScreen(
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
    onDeleteComment: () -> Unit,
    onExpandMenu: (Int, String) -> Unit,
    onChangeIsDialogOpened: (Boolean) -> Unit,
) {
    LaunchedEffect(lazyListState().canScrollBackward) {
        if (lazyListState().canScrollBackward.not()) {
            lazyListState().stopScroll(MutatePriority.PreventUserInput)
        }
    }

    if (isDialogOpened()) {
        TextFieldDialog(
            titleText = { "댓글 수정" },
            description = { "댓글 내용을 수정하세요" },
            onDismiss = { onChangeIsDialogOpened(false) },
            onConfirm = onEditComment,
            initialText = { selectedComment() },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState(),
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BottomSheetDefaults.DragHandle()

                Text(
                    text = "댓글",
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = false,
        ),
        contentWindowInsets = { WindowInsets.ime },
        modifier = Modifier
            .statusBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .fillMaxHeight(if (isImeVisible()) 1f else 0.6f)
                .navigationBarsPadding(),
        ) {
            LazyColumn(
                state = lazyListState(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                            onChangeIsDialogOpened = onChangeIsDialogOpened,
                            onDeleteComment = onDeleteComment,
                            onLongClick = {
                                onExpandMenu(commentItem.id, commentItem.comment)
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            TextField(
                value = comment(),
                placeholder = { Text("댓글 입력") },
                onValueChange = onCommentChange,
                trailingIcon = {
                    if (comment().text.isNotEmpty()) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            IconButton(
                                onClick = onWriteComment,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "댓글 작성",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = TextFieldDefaults.MinHeight,
                        max = TextFieldDefaults.MinHeight * 2,
                    )
                    .padding(vertical = 8.dp, horizontal = 16.dp),
            )
        }
    }
}

@Composable
fun CommentListItem(
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
fun CommentListItemScreen(
    imageLoader: ImageLoader,
    isMenuExpanded: () -> Boolean,
    commentItem: () -> CommentItem,
    onDismissMenu: () -> Unit,
    onLongClick: () -> Unit,
    onChangeIsDialogOpened: (Boolean) -> Unit,
    onDeleteComment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .combinedClickable(
                onClick = { /* TODO: 키보드 내리기, TextField 포커스 해제 */ },
                onLongClick = onLongClick,
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
    ) {
        CircleAsyncImage(
            model = commentItem().userProfileImage,
            contentDescription = "사진",
            imageLoader = imageLoader,
            modifier = Modifier
                .padding(vertical = 6.dp)
                .height(38.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        ) {
            Row {
                Text(
                    text = commentItem().nickname,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 8.dp),
                )

                Text(
                    text = commentItem().date,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }

            Text(
                text = commentItem().comment,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }

        DropdownMenu(
            expanded = isMenuExpanded(),
            onDismissRequest = onDismissMenu,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.Edit, "댓글 수정") },
                text = { Text("댓글 수정하기") },
                onClick = { onChangeIsDialogOpened(true) },
            )
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.Clear, "댓글 삭제") },
                text = { Text("댓글 삭제하기") },
                onClick = onDeleteComment,
            )
        }
    }
}
