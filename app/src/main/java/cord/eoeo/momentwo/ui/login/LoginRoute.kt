package cord.eoeo.momentwo.ui.login

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope

@Composable
fun LoginRoute(
    coroutineScope: CoroutineScope,
    navigateToAlbum: () -> Unit,
    navigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState: LoginContract.State by viewModel.uiState.collectAsStateWithLifecycle()

    LoginScreen(
        coroutineScope = coroutineScope,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.setEvent(event) },
        snackbarHostState = { snackbarHostState },
        navigateToAlbum = navigateToAlbum,
        navigateToSignUp = navigateToSignUp,
    )
}
