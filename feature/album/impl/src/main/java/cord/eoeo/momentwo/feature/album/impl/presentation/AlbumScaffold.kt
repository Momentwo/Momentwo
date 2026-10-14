package cord.eoeo.momentwo.feature.album.impl.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cord.eoeo.momentwo.feature.album.impl.album.AlbumTopAppBar
import cord.eoeo.momentwo.feature.album.impl.album.CreateAlbumFAB
import kotlinx.coroutines.launch

/** Rendering only: navigation and library ownership belong to the calling Route. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlbumScaffold(
    messages: SnackbarHostState,
    create: () -> Unit,
    drawerContent: @Composable (close: () -> Unit) -> Unit,
    drawer: DrawerState = rememberDrawerState(DrawerValue.Closed),
    content: @Composable (PaddingValues) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val close: () -> Unit = { scope.launch { drawer.close() }; Unit }
    BackHandler(drawer.isOpen, close)
    ModalNavigationDrawer(drawerState = drawer, drawerContent = { ModalDrawerSheet { drawerContent(close) } }) {
        Scaffold(topBar = { AlbumTopAppBar({ scope.launch { drawer.open() } }, {}) },
            floatingActionButton = { CreateAlbumFAB(create) },
            snackbarHost = { SnackbarHost(messages) }, content = content)
    }
}

@Composable
internal fun LibraryDrawer(device: () -> Unit, account: () -> Unit, settings: () -> Unit, close: () -> Unit, accountSelected: Boolean = false) {
    listOf(Triple("기기 앨범", !accountSelected, device), Triple("계정 앨범", accountSelected, account), Triple("계정·저장 공간", false, settings)).forEach { (label, selected, navigate) ->
        NavigationDrawerItem(label = { Text(label) }, selected = selected, onClick = { close(); navigate() }, modifier = Modifier.padding(16.dp, 8.dp))
    }
}
