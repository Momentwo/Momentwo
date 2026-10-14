package cord.eoeo.momentwo.feature.album.impl.local

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.feature.album.api.LocalLibraryDestination
import kotlinx.coroutines.launch

fun NavGraphBuilder.localLibraryScreen(navigateToLogin: () -> Unit, navigateToAlbum: (String) -> Unit) {
    composable<LocalLibraryDestination> { LocalLibraryRoute(navigateToLogin, navigateToAlbum) }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalLibraryRoute(navigateToLogin: () -> Unit, navigateToAlbum: (String) -> Unit, vm: LocalLibraryViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val messages = remember { SnackbarHostState() }
    LaunchedEffect(vm) { vm.initialize() }
    CollectEffect(vm.effect) { when (val effect = it) {
        LocalLibraryEffect.Login -> navigateToLogin()
        is LocalLibraryEffect.Album -> navigateToAlbum(effect.id)
        is LocalLibraryEffect.Message -> launch { messages.showSnackbar(effect.text) }
    } }
    if (state.creating) LibraryEditDialog("앨범 만들기", state.draft.text, pending = state.pending, onText = { vm.onEvent(LocalLibraryEvent.TextChanged(it)) }, onSave = { vm.onEvent(LocalLibraryEvent.Save) }, onDismiss = { vm.onEvent(LocalLibraryEvent.Dismiss) })
    Scaffold(topBar = { TopAppBar(title = { Text("내 로컬 앨범") }, actions = { TextButton(onClick = { vm.onEvent(LocalLibraryEvent.ConnectAccount) }) { Text("계정 연결") } }) },
        floatingActionButton = { FloatingActionButton(onClick = { vm.onEvent(LocalLibraryEvent.OpenCreate) }) { Text("앨범 추가", Modifier.padding(12.dp)) } }, snackbarHost = { SnackbarHost(messages) }) { padding ->
        ScreenContent(state.screen, { vm.onEvent(LocalLibraryEvent.Retry) }, Modifier.padding(padding)) { albums ->
            if (albums.isEmpty()) Text("첫 앨범을 만들어보세요", Modifier.padding(24.dp))
            else LazyColumn { items(albums, key = { it.localId }) { album -> ListItem(headlineContent = { Text(album.title) }, supportingContent = { Text("이 기기에 저장됨") }, modifier = Modifier.clickable { vm.onEvent(LocalLibraryEvent.OpenAlbum(album.localId)) }) } }
        }
    }
}
