package cord.eoeo.momentwo.ui.photodetail.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import cord.eoeo.momentwo.ui.composable.CircleAsyncImage
import cord.eoeo.momentwo.core.model.DescriptionItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DescriptionBottomSheet(
    imageLoader: ImageLoader,
    descriptionItem: () -> DescriptionItem,
    isWriteDescMode: () -> Boolean,
    onDismiss: () -> Unit,
    onChangeIsWriteDescMode: (Boolean) -> Unit,
    onWriteDescription: (String, String) -> Unit,
    onEditDescription: (String, String) -> Unit,
    onDeleteDescription: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    val isImeVisible = WindowInsets.isImeVisible
    val descLines = descriptionItem().description.lines()
    val currentTitle = descLines.first()
    val currentContents = descLines.drop(1).joinToString("\n")
    var title by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentTitle))
    }
    var contents by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentContents))
    }
    var contentsMaxLine by rememberSaveable { mutableIntStateOf(4) }

    DescriptionBottomSheetScreen(
        imageLoader = imageLoader,
        bottomSheetState = { bottomSheetState },
        scrollState = { scrollState },
        nestedScrollConnection = { nestedScrollConnection },
        isImeVisible = { isImeVisible },
        isWriteDescMode = isWriteDescMode,
        isLongContents = { descLines.size > 5 },
        title = { title },
        contents = { contents },
        contentsMaxLine = { contentsMaxLine },
        nickname = { descriptionItem().nickname },
        date = { descriptionItem().date },
        profileImage = { descriptionItem().userProfileImage },
        onTitleChange = { title = it },
        onContentsChange = { contents = it },
        onClickContents = { contentsMaxLine = Int.MAX_VALUE },
        onDismiss = onDismiss,
        onClickWrite = {
            onChangeIsWriteDescMode(true)
        },
        onCancelWrite = {
            title = TextFieldValue(currentTitle)
            contents = TextFieldValue(currentContents)
            onChangeIsWriteDescMode(false)
        },
        onWriteDescription = { onWriteDescription(title.text, contents.text) },
        onEditDescription = { onEditDescription(title.text, contents.text) },
        onDeleteDescription = onDeleteDescription,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionBottomSheetScreen(
    imageLoader: ImageLoader,
    bottomSheetState: () -> SheetState,
    scrollState: () -> ScrollState,
    nestedScrollConnection: () -> NestedScrollConnection,
    isImeVisible: () -> Boolean,
    isWriteDescMode: () -> Boolean,
    isLongContents: () -> Boolean,
    title: () -> TextFieldValue,
    contents: () -> TextFieldValue,
    contentsMaxLine: () -> Int,
    nickname: () -> String,
    date: () -> String,
    profileImage: () -> String,
    onTitleChange: (TextFieldValue) -> Unit,
    onContentsChange: (TextFieldValue) -> Unit,
    onClickContents: () -> Unit,
    onDismiss: () -> Unit,
    onClickWrite: () -> Unit,
    onCancelWrite: () -> Unit,
    onWriteDescription: () -> Unit,
    onEditDescription: () -> Unit,
    onDeleteDescription: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState(),
        dragHandle = {
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BottomSheetDefaults.DragHandle()

                    Text(
                        text = "설명",
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) {
                    if (nickname().isEmpty() && isWriteDescMode().not()) {
                        IconButton(onClick = onClickWrite) {
                            Icon(Icons.Default.Edit, "설명 작성")
                        }
                    }

                    if (isWriteDescMode()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            IconButton(onClick = onCancelWrite) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "취소",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (nickname().isEmpty()) {
                                        onWriteDescription()
                                    } else {
                                        onEditDescription()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "확인",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        },
        contentWindowInsets = { WindowInsets.ime },
        modifier = Modifier
            .statusBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .fillMaxHeight(if (isWriteDescMode() && isImeVisible()) 1f else 0.45f)
                .navigationBarsPadding(),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (isWriteDescMode()) {
                WriteDescriptionScreen(
                    title = title,
                    contents = contents,
                    onTitleChange = onTitleChange,
                    onContentsChange = onContentsChange,
                )
            } else {
                DescriptionScreen(
                    imageLoader = imageLoader,
                    scrollState = scrollState,
                    nestedScrollConnection = nestedScrollConnection,
                    title = { title().text },
                    contents = { contents().text },
                    contentsMaxLine = contentsMaxLine,
                    nickname = nickname,
                    date = date,
                    profileImage = profileImage,
                    isWriteDescMode = isWriteDescMode,
                    isLongContents = isLongContents,
                    onDismiss = onDismiss,
                    onClickContents = onClickContents,
                    onClickWrite = onClickWrite,
                    onDeleteDescription = onDeleteDescription,
                )
            }
        }
    }
}

@Composable
fun DescriptionScreen(
    imageLoader: ImageLoader,
    scrollState: () -> ScrollState,
    nestedScrollConnection: () -> NestedScrollConnection,
    title: () -> String,
    contents: () -> String,
    contentsMaxLine: () -> Int,
    nickname: () -> String,
    date: () -> String,
    profileImage: () -> String,
    isWriteDescMode: () -> Boolean,
    isLongContents: () -> Boolean,
    onDismiss: () -> Unit,
    onClickContents: () -> Unit,
    onClickWrite: () -> Unit,
    onDeleteDescription: () -> Unit,
) {
    LaunchedEffect(scrollState().canScrollBackward) {
        if (scrollState().canScrollBackward.not()) {
            scrollState().stopScroll(MutatePriority.PreventUserInput)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState())
            .nestedScroll(nestedScrollConnection())
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
    ) {
        if (nickname().isNotEmpty()) {
            Text(
                text = title(),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(0.55f)
                        .height(intrinsicSize = IntrinsicSize.Max),
                ) {
                    CircleAsyncImage(
                        model = profileImage(),
                        contentDescription = "사진",
                        imageLoader = imageLoader,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp),
                    ) {
                        Text(
                            text = "작성자",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            maxLines = 1,
                        )

                        Text(
                            text = nickname(),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(0.45f),
                ) {
                    Text(
                        text = "작성일",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                    )

                    Text(
                        text = date(),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        onClick = onClickContents,
                    ),
            ) {
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            text = contents(),
                            style = LocalTextStyle.current.copy(

                            ),
                            maxLines = contentsMaxLine(),
                        )

                        if (isLongContents() && contentsMaxLine() == 4) {
                            Text(
                                text = "...더보기",
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onClickWrite) {
                            Icon(Icons.Default.Edit, "설명 수정")
                        }

                        IconButton(onClick = onDeleteDescription) {
                            Icon(Icons.Default.Clear, "설명 삭제")
                        }
                    }
                }
            }
        } else {
            Text(
                text = "작성한 설명이 없습니다",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun WriteDescriptionScreen(
    title: () -> TextFieldValue,
    contents: () -> TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    onContentsChange: (TextFieldValue) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(),
    ) {
        TextField(
            value = title(),
            onValueChange = onTitleChange,
            placeholder = { Text("제목 입력") },
            colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            textStyle = LocalTextStyle.current.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        TextField(
            value = contents(),
            onValueChange = onContentsChange,
            placeholder = { Text("내용 입력") },
            colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp),
        )
    }
}
