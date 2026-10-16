package cord.eoeo.momentwo.feature.albumdetail.impl

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlbumDetailRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    navigateToPhotoList: (Int, Int, String, String) -> Unit,
    subAlbumContent: @Composable (SubAlbumContentState, SubAlbumContentActions) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navActions = remember(navController) { AlbumDetailNavigationActions(navController) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val uiState: AlbumDetailState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.initialize() }

    AlbumDetailScreen(
        coroutineScope = coroutineScope,
        navController = navController,
        navActions = navActions,
        scrollBehavior = scrollBehavior,
        imageLoader = imageLoader,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.onEvent(event) },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
        navigateToPhotoList = navigateToPhotoList,
        subAlbumContent = subAlbumContent,
    )
}
