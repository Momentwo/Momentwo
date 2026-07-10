package cord.eoeo.momentwo.ui.friend

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import cord.eoeo.momentwo.core.model.FriendItem
import kotlinx.coroutines.CoroutineScope

@Composable
fun FriendRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: FriendViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navActions = remember(navController) { FriendNavigationActions(navController) }
    val uiState: FriendContract.State by viewModel.uiState.collectAsStateWithLifecycle()
    val friendPagingData: LazyPagingItems<FriendItem> = uiState.friendPagingData.collectAsLazyPagingItems()

    FriendScreen(
        coroutineScope = coroutineScope,
        imageLoader = imageLoader,
        navController = navController,
        navActions = navActions,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.setEvent(event) },
        friendPagingData = { friendPagingData },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
    )
}
