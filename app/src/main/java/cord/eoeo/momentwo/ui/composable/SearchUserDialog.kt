package cord.eoeo.momentwo.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.ImageLoader
import coil.imageLoader
import cord.eoeo.momentwo.core.model.UserItem
import cord.eoeo.momentwo.core.designsystem.theme.MomentwoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun SearchUserDialog(
    imageLoader: ImageLoader,
    searchResult: () -> Flow<PagingData<UserItem>>,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onRequestFriend: (String) -> Unit,
) {
    val lazySearchResult = searchResult().collectAsLazyPagingItems()
    var query: String by rememberSaveable { mutableStateOf("") }
    var selectedUser: String by rememberSaveable { mutableStateOf("") }
    var isAlertOpened: Boolean by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(query) {
        delay(500L)
        if (query.isNotEmpty()) {
            onSearch(query)
        }
    }

    SearchUserDialogScreen(
        imageLoader = imageLoader,
        lazySearchResult = { lazySearchResult },
        query = { query },
        selectedUser = { selectedUser },
        isAlertOpened = { isAlertOpened },
        onQueryChange = { query = it },
        onDismissAlert = { isAlertOpened = false },
        onConfirmAlert = {
            isAlertOpened = false
            onRequestFriend(selectedUser)
        },
        onSearch = onSearch,
        onDismiss = onDismiss,
        onSelectUser = { nickname ->
            selectedUser = nickname
            isAlertOpened = true
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserDialogScreen(
    imageLoader: ImageLoader,
    lazySearchResult: () -> LazyPagingItems<UserItem>,
    query: () -> String,
    selectedUser: () -> String,
    isAlertOpened: () -> Boolean,
    onQueryChange: (String) -> Unit,
    onDismissAlert: () -> Unit,
    onConfirmAlert: () -> Unit,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onSelectUser: (String) -> Unit,
) {
    if (isAlertOpened()) {
        SearchUserAlertDialog(
            nickname = selectedUser,
            onClickConfirm = onConfirmAlert,
            onClickDismiss = onDismissAlert,
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
            ),
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier =
                Modifier
                    .fillMaxHeight(0.8f)
                    .fillMaxWidth(0.9f),
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query(),
                        onQueryChange = onQueryChange,
                        onSearch = onSearch,
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(text = "사용자 검색") },
                        leadingIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, "")
                            }
                        },
                    )
                },
                expanded = true,
                onExpandedChange = { },
                modifier = Modifier.fillMaxWidth(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        count = lazySearchResult().itemCount,
                        key = lazySearchResult().itemKey { it.id },
                    ) { index ->
                        val userItem = lazySearchResult()[index]

                        userItem?.let { user ->
                            ListItem(
                                leadingContent = {
                                    CircleAsyncImage(
                                        model = user.userProfileImage,
                                        imageLoader = imageLoader,
                                        contentDescription = "유저 프로필 이미지",
                                        modifier = Modifier.height(45.dp),
                                    )
                                },
                                headlineContent = {
                                    Text(user.nickname)
                                },
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                        .clickable { onSelectUser(user.nickname) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchUserAlertDialog(
    nickname: () -> String,
    onClickConfirm: () -> Unit,
    onClickDismiss: () -> Unit,
) {
    AlertDialog(
        icon = { Icon(Icons.Default.PersonAdd, "") },
        title = { Text("친구 요청") },
        text = {
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(nickname())
                    }
                    append("님에게 친구 요청을 보내시겠습니까?")
                },
            )
        },
        onDismissRequest = onClickDismiss,
        confirmButton = {
            TextButton(onClick = onClickConfirm) {
                Text("요청")
            }
        },
        dismissButton = {
            TextButton(onClick = onClickDismiss) {
                Text(
                    text = "취소",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}

@Preview
@Composable
private fun SearchUserDialogPreview() {
    MomentwoTheme {
        SearchUserDialog(
            imageLoader = LocalContext.current.imageLoader,
            searchResult = {
                flowOf(
                    PagingData.from(
                        listOf(
                            UserItem(1, "User1", ""),
                            UserItem(2, "User2", ""),
                            UserItem(3, "User3", ""),
                            UserItem(4, "User4", ""),
                            UserItem(5, "User5", ""),
                        ),
                    ),
                )
            },
            onSearch = { },
            onDismiss = { },
            onRequestFriend = { },
        )
    }
}

@Preview
@Composable
private fun SearchUserAlertDialogPreview() {
    MomentwoTheme {
        SearchUserAlertDialog(
            nickname = { "User1" },
            onClickConfirm = { },
            onClickDismiss = { },
        )
    }
}
