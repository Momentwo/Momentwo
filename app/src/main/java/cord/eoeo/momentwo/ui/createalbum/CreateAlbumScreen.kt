package cord.eoeo.momentwo.ui.createalbum

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleStartEffect
import cord.eoeo.momentwo.ui.SIDE_EFFECTS_KEY
import cord.eoeo.momentwo.ui.START_EFFECTS_KEY
import cord.eoeo.momentwo.ui.composable.InviteDialog
import cord.eoeo.momentwo.ui.composable.UserItemBox
import cord.eoeo.momentwo.ui.model.UserItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreateAlbumScreen(
    coroutineScope: CoroutineScope,
    title: () -> String,
    onTitleChange: (String) -> Unit,
    uiState: () -> CreateAlbumContract.State,
    effectFlow: () -> Flow<CreateAlbumContract.Effect>,
    onEvent: (event: CreateAlbumContract.Event) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LifecycleStartEffect(START_EFFECTS_KEY) {
        onEvent(CreateAlbumContract.Event.OnGetFriendList)
        onStopOrDispose { }
    }

    LaunchedEffect(SIDE_EFFECTS_KEY) {
        effectFlow()
            .onEach { effect ->
                when (effect) {
                    is CreateAlbumContract.Effect.PopBackStack -> {
                        popBackStack()
                    }

                    is CreateAlbumContract.Effect.ShowSnackbar -> {
                        snackbarHostState().showSnackbar(
                            message = effect.message,
                        )
                    }
                }
            }.collect()
    }

    if (uiState().isInviteFriendOpened) {
        InviteDialog(
            friendItemList = { uiState().friendList },
            selectedFriendList = { uiState().tempSelectedFriendList },
            onClickClear = { onEvent(CreateAlbumContract.Event.OnCheckedChange(false, it)) },
            onCheckedChange = { isChecked, friendItem ->
                onEvent(
                    CreateAlbumContract.Event.OnCheckedChange(
                        isChecked,
                        friendItem,
                    ),
                )
            },
            getIsChecked = { uiState().tempSelectedFriendMap[it] ?: false },
            onDismiss = { onEvent(CreateAlbumContract.Event.OnDismissInviteFriend) },
            onConfirm = { onEvent(CreateAlbumContract.Event.OnConfirmInviteFriend) },
        )
    }

    Scaffold(
        topBar = { CreateAlbumTopAppBar(onClickNavigation = { onEvent(CreateAlbumContract.Event.OnBack) }) },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .padding(top = 8.dp)
                    .padding(horizontal = 24.dp),
        ) {
            TitleText(text = { "앨범명" })
            CaptionText(text = { "앨범을 나타낼 수 있는 이름을 지어주세요" })
            OutlinedTextField(
                value = title(),
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Column(
                    modifier =
                        Modifier
                            .weight(1f),
                ) {
                    TitleText(text = { "친구 초대" })
                    CaptionText(text = { "앨범을 같이 사용할 친구를 초대하세요" })
                }
                ElevatedButton(
                    onClick = { onEvent(CreateAlbumContract.Event.OnClickInviteFriend) },
                    modifier =
                        Modifier
                            .align(Alignment.CenterVertically),
                ) {
                    Icon(Icons.Default.Add, "")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "추가")
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(80.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .weight(1f),
            ) {
                items(items = uiState().selectedFriendList, key = { it.nickname }) { friendItem ->
                    UserItemBox(
                        userItem = { UserItem(0, friendItem.nickname) },
                        onClickClear = { onEvent(CreateAlbumContract.Event.OnClearFriendItem(it)) },
                        modifier =
                            Modifier
                                .animateItemPlacement()
                                .fillMaxWidth(),
                    )
                }
            }

            ConfirmExtendedFAB(
                onClick = { /*TODO: Create Album & Navigate to Album*/ },
                modifier =
                    Modifier
                        .align(Alignment.End)
                        .padding(vertical = 16.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlbumTopAppBar(onClickNavigation: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Create Album",
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = {
            IconButton(onClick = onClickNavigation) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "")
            }
        },
    )
}

@Composable
fun TitleText(text: () -> String) {
    Text(
        text = text(),
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        maxLines = 1,
    )
}

@Composable
fun CaptionText(text: () -> String) {
    Text(
        text = text(),
        fontWeight = FontWeight.Light,
        fontSize = 15.sp,
        maxLines = 1,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
fun ConfirmExtendedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        text = { Text(text = "완료") },
        icon = { Icon(Icons.Default.Check, "") },
        onClick = onClick,
        elevation = FloatingActionButtonDefaults.loweredElevation(),
        modifier = modifier,
    )
}
