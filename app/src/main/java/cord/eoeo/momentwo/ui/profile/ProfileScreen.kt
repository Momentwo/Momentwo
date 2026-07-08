package cord.eoeo.momentwo.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.imageLoader
import cord.eoeo.momentwo.core.common.SIDE_EFFECTS_KEY
import cord.eoeo.momentwo.core.designsystem.theme.MomentwoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    uiState: () -> ProfileContract.State,
    effectFlow: () -> Flow<ProfileContract.Effect>,
    onEvent: (event: ProfileContract.Event) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        effectFlow()
            .onEach { effect ->
                when (effect) {
                    is ProfileContract.Effect.PopBackStack -> {
                        popBackStack()
                    }

                    is ProfileContract.Effect.ShowSnackbar -> {
                        coroutineScope.launch {
                            snackbarHostState().showSnackbar(
                                message = effect.message,
                            )
                        }
                    }
                }
            }.collect()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState()) },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .padding(16.dp),
        ) {
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    MomentwoTheme {
        ProfileScreen(
            coroutineScope = rememberCoroutineScope(),
            imageLoader = LocalContext.current.imageLoader,
            uiState = { ProfileContract.State() },
            effectFlow = { emptyFlow() },
            onEvent = { },
            snackbarHostState = { SnackbarHostState() },
            popBackStack = { },
        )
    }
}
