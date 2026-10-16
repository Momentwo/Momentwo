package cord.eoeo.momentwo.feature.auth.impl.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.imageLoader
import cord.eoeo.momentwo.core.ui.CollectEffect
import cord.eoeo.momentwo.core.designsystem.theme.MomentwoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
internal fun ProfileScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    uiState: () -> ProfileState,
    effectFlow: () -> Flow<ProfileEffect>,
    onEvent: (event: ProfileEvent) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CollectEffect(effectFlow()) { effect ->
        when (effect) {
            ProfileEffect.PopBackStack -> popBackStack()
            is ProfileEffect.ShowSnackbar -> launch { snackbarHostState().showSnackbar(effect.message) }
        }
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
            uiState = { ProfileState() },
            effectFlow = { emptyFlow() },
            onEvent = { },
            snackbarHostState = { SnackbarHostState() },
            popBackStack = { },
        )
    }
}
