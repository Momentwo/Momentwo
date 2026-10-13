package cord.eoeo.momentwo.feature.friend.impl

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.ui.rememberPagingScreenState
import cord.eoeo.momentwo.core.data.toUiError
import kotlinx.coroutines.flow.emptyFlow
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
internal fun FriendRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: FriendViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navActions = remember(navController) { FriendNavigationActions(navController) }
    val uiState: FriendState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.initialize() }
    val source = (uiState.friendPagingSource as? ScreenState.Success)?.content ?: remember { emptyFlow() }
    val friendPagingData = source.collectAsLazyPagingItems()
    val pagingState = rememberPagingScreenState(friendPagingData, Throwable::toUiError)
    val screenState = when (val ready = uiState.friendPagingSource) {
        ScreenState.Loading -> ScreenState.Loading
        is ScreenState.Error -> ScreenState.Error(ready.error)
        is ScreenState.Success -> pagingState
    }
    var refreshedRevision by remember(viewModel) { mutableLongStateOf(0) }
    LaunchedEffect(uiState.selectedNavIndex, uiState.friendListRevision) {
        if (uiState.selectedNavIndex == 0 && uiState.friendListRevision > refreshedRevision) {
            friendPagingData.refresh()
            refreshedRevision = uiState.friendListRevision
        }
    }

    FriendScreen(
        coroutineScope = coroutineScope,
        imageLoader = imageLoader,
        navController = navController,
        navActions = navActions,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.onEvent(event) },
        friendPagingData = { friendPagingData },
        pagingState = { screenState },
        retryPagingSource = { viewModel.initialize() },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
    )
}
