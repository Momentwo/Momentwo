package cord.eoeo.momentwo.ui.photolist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import cord.eoeo.momentwo.ui.model.ImageItem
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: PhotoListViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val uiState: PhotoListContract.State by viewModel.uiState.collectAsStateWithLifecycle()
    val refreshState: PullToRefreshState = rememberPullToRefreshState()
    val photoPagingData: LazyPagingItems<ImageItem> = uiState.photoPagingData.collectAsLazyPagingItems()
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri ->
            viewModel.setEvent(
                PhotoListContract.Event.OnUploadImage(
                    imageUri,
                    contentResolver.getType(imageUri) ?: ""
                )
            )
        }
    }

    PhotoListScreen(
        coroutineScope = coroutineScope,
        imageLoader = imageLoader,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.setEvent(event) },
        refreshState = { refreshState },
        photoPagingData = { photoPagingData },
        launchGallery = { galleryLauncher.launch("image/*") },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
    )
}
