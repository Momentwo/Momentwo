package cord.eoeo.momentwo.ui.createalbum

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
fun CreateAlbumRoute(
    coroutineScope: CoroutineScope,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: CreateAlbumViewModel = hiltViewModel(),
) {
    val uiState: CreateAlbumContract.State by viewModel.uiState.collectAsStateWithLifecycle()
    var title: String by rememberSaveable { mutableStateOf("") }

    CreateAlbumScreen(
        coroutineScope = coroutineScope,
        title = { title },
        onTitleChange = { title = it },
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.setEvent(event) },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
    )
}
