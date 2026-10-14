package cord.eoeo.momentwo.feature.album.impl.local

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
import cord.eoeo.momentwo.core.ui.CollectEffect
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.feature.album.api.LocalLibraryDestination

fun NavGraphBuilder.localLibraryScreen(navigateToLogin: () -> Unit) {
    composable<LocalLibraryDestination> { LocalLibraryRoute(navigateToLogin) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalLibraryRoute(navigateToLogin: () -> Unit, vm: LocalLibraryViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(vm) { vm.initialize() }
    CollectEffect(vm.effect) { when (it) { LocalLibraryEffect.Login -> navigateToLogin() } }
    Scaffold(topBar = { TopAppBar(title = { Text("내 로컬 앨범") }, actions = { TextButton(onClick = { vm.onEvent(LocalLibraryEvent.ConnectAccount) }) { Text("계정 연결") } }) }) { padding ->
        ScreenContent(state.screen, { vm.onEvent(LocalLibraryEvent.Retry) }, Modifier.padding(padding)) { albums ->
            if (albums.isEmpty()) Text("아직 앨범이 없습니다", Modifier.padding(24.dp))
            else LazyColumn { items(albums, key = { it.localId }) { ListItem(headlineContent = { Text(it.title) }, supportingContent = { Text("이 기기에 저장됨") }) } }
        }
    }
}
