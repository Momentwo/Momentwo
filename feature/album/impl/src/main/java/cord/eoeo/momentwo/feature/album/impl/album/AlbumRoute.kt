package cord.eoeo.momentwo.feature.album.impl.album

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import cord.eoeo.momentwo.core.model.AlbumItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun AlbumRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    navigateToCreateAlbum: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToFriend: () -> Unit,
    navigateToAlbumDetail: (AlbumItem) -> Unit,
    device: () -> Unit = {},
    account: () -> Unit = {},
    settings: () -> Unit = {},
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val uiState: AlbumState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleStartEffect(viewModel) { viewModel.initialize(); onStopOrDispose { } }

    AlbumScreen(
        coroutineScope = coroutineScope,
        imageLoader = imageLoader,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.onEvent(event) },
        drawerState = { drawerState },
        snackbarHostState = { snackbarHostState },
        onClickDrawer = { coroutineScope.launch { drawerState.open() } },
        navigateToCreateAlbum = { viewModel.onEvent(AlbumEvent.OnInvalidate); navigateToCreateAlbum() },
        device = device, account = account, settings = settings,
        navigateToProfile = navigateToProfile,
        navigateToFriend = navigateToFriend,
        navigateToAlbumDetail = { item -> viewModel.onEvent(AlbumEvent.OnInvalidate); navigateToAlbumDetail(item) },
    )
}
