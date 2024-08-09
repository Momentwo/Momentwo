package cord.eoeo.momentwo.ui.friend

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope

@Composable
fun FriendRoute(
    coroutineScope: CoroutineScope,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: FriendViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navActions = remember(navController) { FriendNavigationActions(navController) }
    val uiState: FriendContract.State by viewModel.uiState.collectAsStateWithLifecycle()

    FriendScreen(
        coroutineScope = coroutineScope,
        navController = navController,
        navActions = navActions,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.setEvent(event) },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
    )
}
