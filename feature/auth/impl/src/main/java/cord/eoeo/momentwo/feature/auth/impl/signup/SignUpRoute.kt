package cord.eoeo.momentwo.feature.auth.impl.signup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SignUpRoute(
    coroutineScope: CoroutineScope,
    popBackStack: () -> Unit,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val uiState: SignUpState by viewModel.uiState.collectAsStateWithLifecycle()

    SignUpScreen(
        coroutineScope = coroutineScope,
        pagerState = { pagerState },
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.onEvent(event) },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
        navigateToLogin = navigateToLogin,
    )
}
