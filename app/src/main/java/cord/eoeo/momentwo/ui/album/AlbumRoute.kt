package cord.eoeo.momentwo.ui.album

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import cord.eoeo.momentwo.ui.model.AlbumItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AlbumRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    navigateToCreateAlbum: () -> Unit,
    navigateToFriend: () -> Unit,
    navigateToAlbumDetail: (AlbumItem) -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val uiState: AlbumContract.State by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumScreen(
        coroutineScope = coroutineScope,
        imageLoader = imageLoader,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.setEvent(event) },
        drawerState = { drawerState },
        snackbarHostState = { snackbarHostState },
        onClickDrawer = { coroutineScope.launch { drawerState.open() } },
        onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
        navigateToCreateAlbum = navigateToCreateAlbum,
        navigateToFriend = navigateToFriend,
        navigateToAlbumDetail = navigateToAlbumDetail,
    )
}
