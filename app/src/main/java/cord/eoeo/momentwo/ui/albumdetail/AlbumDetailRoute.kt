package cord.eoeo.momentwo.ui.albumdetail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    navigateToPhotoList: (Int, Int, String, String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navActions = remember(navController) { AlbumDetailNavigationActions(navController) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val uiState: AlbumDetailContract.State by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumDetailScreen(
        coroutineScope = coroutineScope,
        navController = navController,
        navActions = navActions,
        scrollBehavior = scrollBehavior,
        imageLoader = imageLoader,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.setEvent(event) },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
        navigateToPhotoList = navigateToPhotoList,
    )
}
