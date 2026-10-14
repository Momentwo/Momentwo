package cord.eoeo.momentwo.feature.album.impl.local

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.feature.album.api.LocalLibraryDestination
import cord.eoeo.momentwo.feature.album.impl.presentation.*
import kotlinx.coroutines.launch

fun NavGraphBuilder.localLibraryScreen(imageLoader: ImageLoader, navigateToLogin: () -> Unit, navigateToAlbum: (String) -> Unit, create: () -> Unit, account: () -> Unit) {
    composable<LocalLibraryDestination> { LocalLibraryRoute(imageLoader, navigateToLogin, navigateToAlbum, create, account) }
}
@Composable
private fun LocalLibraryRoute(loader: ImageLoader, settings: () -> Unit, open: (String) -> Unit, create: () -> Unit, account: () -> Unit, vm: LocalLibraryViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val messages = remember { SnackbarHostState() }
    LaunchedEffect(vm) { vm.initialize() }
    CollectEffect(vm.effect) { when (val effect = it) {
        LocalLibraryEffect.Login -> settings()
        is LocalLibraryEffect.Album -> open(effect.id)
        is LocalLibraryEffect.Message -> launch { messages.showSnackbar(effect.text) }
    } }
    AlbumScaffold(messages, create, { close -> LibraryDrawer({}, account, settings, close) }) { padding ->
        ScreenContent(state.screen, { vm.onEvent(LocalLibraryEvent.Retry) }, Modifier.padding(padding)) { albums ->
            if (albums.isEmpty()) EmptyContent() else LazyVerticalGrid(GridCells.Adaptive(160.dp), Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                items(albums, key = { it.localId }) { album ->
                    AlbumItemCard(loader, { album.coverUri }, { album.title }, { album.subtitle }, { open(album.localId) }, Modifier.animateItem())
                }
            }
        }
    }
}
