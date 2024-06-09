package cord.eoeo.momentwo.ui.signup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SignUpRoute(
    coroutineScope: CoroutineScope,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val uiState: SignUpContract.State by viewModel.uiState.collectAsStateWithLifecycle()

    SignUpScreen(
        coroutineScope = coroutineScope,
        pagerState = { pagerState },
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.setEvent(event) },
        navigateToLogin = navigateToLogin,
    )
}
