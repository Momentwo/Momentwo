package cord.eoeo.momentwo.feature.album.impl.account
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import cord.eoeo.momentwo.core.common.RefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.feature.album.api.AccountAlbumsDestination
import cord.eoeo.momentwo.feature.album.impl.presentation.*
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.accountAlbumsScreen(loader: ImageLoader, device: () -> Unit, settings: () -> Unit, create: () -> Unit, open: (String) -> Unit) {
    composable<AccountAlbumsDestination> {
        val vm: AccountLibraryViewModel = hiltViewModel()
        val state by vm.uiState.collectAsStateWithLifecycle()
        val messages = remember { SnackbarHostState() }
        LaunchedEffect(vm) { vm.openAlbums() }
        CollectEffect(vm.effect) { if (it is AccountLibraryEffect.Message) launch { messages.showSnackbar(it.text) } }
        AlbumScaffold(messages, { if ((state.screen as? cord.eoeo.momentwo.core.common.ScreenState.Success)?.content?.identity != null) create() else settings() },
            { close -> LibraryDrawer(device, {}, settings, close, true) }) { padding ->
            ScreenContent(state.screen, { vm.onEvent(AccountLibraryEvent.Retry) }, Modifier.padding(padding)) { content ->
                if (content.identity == null) Column(Modifier.padding(24.dp)) { Text("클라우드·공유를 이용하려면 계정을 연결해주세요")
                    TextButton(onClick = settings) { Text("계정 연결") } }
                else PullToRefreshBox(isRefreshing=(state.screen as? cord.eoeo.momentwo.core.common.ScreenState.Success)?.refresh==RefreshState.Loading,
                    onRefresh={vm.onEvent(AccountLibraryEvent.Retry)},modifier=Modifier.fillMaxSize()) {
                    if (content.albums.isEmpty()) EmptyContent() else LazyVerticalGrid(GridCells.Adaptive(160.dp), Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                        items(content.albums, key = { it.localId }) { album -> AlbumItemCard(loader, { album.coverUri }, { album.title }, { album.subtitle }, { open(album.localId) }, Modifier.animateItem()) }
                    }
                }
            }
        }
    }
}
