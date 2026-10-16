package cord.eoeo.momentwo.feature.auth.impl.login

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun LoginRoute(
    coroutineScope: CoroutineScope,
    navigateToAlbum: () -> Unit,
    navigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState: LoginState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.initialize() }

    LoginScreen(
        coroutineScope = coroutineScope,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.onEvent(event) },
        snackbarHostState = { snackbarHostState },
        navigateToAlbum = navigateToAlbum,
        navigateToSignUp = navigateToSignUp,
    )
}
