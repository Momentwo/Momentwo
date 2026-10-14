package cord.eoeo.momentwo.feature.album.impl.createalbum

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cord.eoeo.momentwo.core.ui.CollectEffect
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.core.common.ScreenState
import androidx.compose.ui.window.Dialog
import cord.eoeo.momentwo.core.ui.InviteDialog
import cord.eoeo.momentwo.core.ui.UserItemBox
import cord.eoeo.momentwo.core.model.UserItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@Composable
internal fun CreateAlbumScreen(
    coroutineScope: CoroutineScope,
    title: () -> String,
    onTitleChange: (String) -> Unit,
    uiState: () -> CreateAlbumState,
    effectFlow: () -> Flow<CreateAlbumEffect>,
    onEvent: (event: CreateAlbumEvent) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CollectEffect(effectFlow()) { effect ->
        when (effect) {
            CreateAlbumEffect.PopBackStack -> popBackStack()
            is CreateAlbumEffect.ShowSnackbar -> launch { snackbarHostState().showSnackbar(effect.message) }
        }
    }

    if (uiState().isInviteFriendOpened) {
        when (val friends = uiState().friends) {
            ScreenState.Loading, is ScreenState.Error -> Dialog(onDismissRequest = { onEvent(CreateAlbumEvent.OnDismissInviteFriend) }) {
                ScreenContent(friends, { onEvent(CreateAlbumEvent.OnGetFriendList) }, Modifier.fillMaxSize()) { }
            }
            is ScreenState.Success -> InviteDialog(
                friendItemList = { friends.content }, selectedFriendList = { uiState().tempSelectedFriendList },
                onCheckedChange = { checked, friend -> onEvent(CreateAlbumEvent.OnCheckedChange(checked, friend)) },
                onDismiss = { onEvent(CreateAlbumEvent.OnDismissInviteFriend) },
                onConfirm = { onEvent(CreateAlbumEvent.OnConfirmInviteFriend) },
                getIsChecked = { uiState().isChecked(it) },
            )
        }
    }

    CreateAlbumContent(title(), onTitleChange, uiState().isLoading, { onEvent(CreateAlbumEvent.OnBack) },
        { onEvent(CreateAlbumEvent.OnClickInviteFriend) }, { onEvent(CreateAlbumEvent.OnRequestCreateAlbum(title())) }, snackbarHostState()) {
        LazyVerticalGrid(columns = GridCells.Adaptive(80.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            items(uiState().selectedFriendList, key = { it.nickname }) { friend ->
                UserItemBox(userItem = { UserItem(0, friend.nickname, friend.userProfileImage) },
                    onClickClear = { onEvent(CreateAlbumEvent.OnClearFriendItem(it)) }, modifier = Modifier.animateItem().fillMaxWidth())
            }
        }
    }
}

@Composable
internal fun CreateAlbumContent(
    title: String, onTitleChange: (String) -> Unit, pending: Boolean,
    back: () -> Unit, invite: () -> Unit, submit: () -> Unit,
    messages: SnackbarHostState, invitedContent: @Composable () -> Unit,
) {
    Scaffold(
        topBar = { CreateAlbumTopAppBar(onClickNavigation = { back() }) },
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { androidx.compose.material3.SnackbarHost(messages) },
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
                value = title,
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
                    onClick = { invite() },
                    modifier =
                        Modifier
                            .align(Alignment.CenterVertically),
                ) {
                    Icon(Icons.Default.Add, "")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "추가")
                }
            }
            Column(Modifier.weight(1f)) { invitedContent() }

            ConfirmExtendedFAB(
                enabled = !pending,
                onClick = { submit() },
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
internal fun CreateAlbumTopAppBar(onClickNavigation: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Create Album",
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = {
            IconButton(onClick = onClickNavigation) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
            }
        },
    )
}

@Composable
internal fun TitleText(text: () -> String) {
    Text(
        text = text(),
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        maxLines = 1,
    )
}

@Composable
internal fun CaptionText(text: () -> String) {
    Text(
        text = text(),
        fontWeight = FontWeight.Light,
        fontSize = 15.sp,
        maxLines = 1,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
internal fun ConfirmExtendedFAB(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        text = { Text(text = if (enabled) "완료" else "생성 중") },
        icon = { Icon(Icons.Default.Check, "") },
        onClick = { if (enabled) onClick() },
        elevation = FloatingActionButtonDefaults.loweredElevation(),
        modifier = modifier.semantics { if (!enabled) disabled() },
    )
}
