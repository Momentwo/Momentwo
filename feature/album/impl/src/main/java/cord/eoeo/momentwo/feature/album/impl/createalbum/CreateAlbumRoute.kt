package cord.eoeo.momentwo.feature.album.impl.createalbum

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun CreateAlbumRoute(
    coroutineScope: CoroutineScope,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: CreateAlbumViewModel = hiltViewModel(),
) {
    val uiState: CreateAlbumState by viewModel.uiState.collectAsStateWithLifecycle()

    CreateAlbumScreen(
        coroutineScope = coroutineScope,
        title = { uiState.title },
        onTitleChange = { viewModel.onEvent(CreateAlbumEvent.OnTitleChanged(it)) },
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.onEvent(event) },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
    )
}
