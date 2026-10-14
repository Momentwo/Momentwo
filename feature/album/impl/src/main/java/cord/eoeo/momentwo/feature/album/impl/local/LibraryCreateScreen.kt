package cord.eoeo.momentwo.feature.album.impl.local
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.ui.window.Dialog
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.model.FriendItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.feature.album.api.LibraryCreateAlbumDestination
import cord.eoeo.momentwo.feature.album.impl.createalbum.CreateAlbumContent
import kotlinx.coroutines.launch
fun NavGraphBuilder.libraryCreateAlbumScreen(back: () -> Unit, connect: () -> Unit, created: (Boolean) -> Unit) {
    composable<LibraryCreateAlbumDestination> {
        val vm: LibraryCreateViewModel = hiltViewModel()
        val state by vm.uiState.collectAsStateWithLifecycle()
        val messages = remember { SnackbarHostState() }
        var guide by remember { mutableStateOf(false) }
        LaunchedEffect(vm) { vm.initialize() }
        CollectEffect(vm.effect) { when (it) { LibraryCreateEffect.Back -> back(); is LibraryCreateEffect.Created -> created(it.account); LibraryCreateEffect.Connect -> guide = true; is LibraryCreateEffect.Message -> launch { messages.showSnackbar(it.text) } } }
        if (guide) AlertDialog(onDismissRequest = { guide = false }, title = { Text("친구와 앨범 공유") },
            text = { Text("기기 앨범은 나만 사용할 수 있습니다. 계정을 연결한 뒤 선택한 앨범을 복사하면 친구를 초대할 수 있습니다. 작성 중인 내용과 기기 원본은 유지됩니다.") },
            confirmButton = { TextButton(onClick = { guide = false; connect() }) { Text("계정 연결") } }, dismissButton = { TextButton(onClick = { guide = false }) { Text("계속 작성") } })
        if (state.inviteOpen) when (val friends = state.friends) {
            ScreenState.Loading, is ScreenState.Error -> Dialog(onDismissRequest = { vm.onEvent(LibraryCreateEvent.DismissInvite) }) {
                ScreenContent(friends, { vm.onEvent(LibraryCreateEvent.Invite) }, Modifier.fillMaxSize()) { }
            }
            is ScreenState.Success -> InviteDialog({ friends.content },
                { state.temporary.map { name -> friends.content.firstOrNull { it.nickname == name } ?: FriendItem(name, "") } },
                { it.nickname in state.temporary }, { checked, friend -> vm.onEvent(LibraryCreateEvent.Check(friend.nickname, checked)) },
                { vm.onEvent(LibraryCreateEvent.DismissInvite) }, { vm.onEvent(LibraryCreateEvent.ConfirmInvite) })
        }
        CreateAlbumContent(if(state.ownerAvailable)state.draft.text else "", { vm.onEvent(LibraryCreateEvent.Text(it)) }, state.pending || !state.ready || !state.ownerAvailable,
            { vm.onEvent(LibraryCreateEvent.Back) }, { vm.onEvent(LibraryCreateEvent.Invite) }, { vm.onEvent(LibraryCreateEvent.Submit) }, messages, inputEnabled=state.ready && state.ownerAvailable) {
            if(state.restoreFailed)TextButton(onClick={vm.onEvent(LibraryCreateEvent.Retry)}){Text("다시 시도")}
            if(!state.ownerAvailable)Text("작성하던 계정으로 다시 연결하면 이어서 만들 수 있습니다")
            LazyVerticalGrid(GridCells.Adaptive(80.dp), Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                items(if(state.ownerAvailable)state.selected else emptyList(), key = { it }) { name -> FriendItemBox(name, { vm.onEvent(LibraryCreateEvent.Toggle(name)) }, Modifier.animateItem().fillMaxWidth()) }
            }
        }
    }
}
