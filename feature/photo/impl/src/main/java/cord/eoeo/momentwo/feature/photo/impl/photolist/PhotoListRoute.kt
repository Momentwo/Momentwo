package cord.eoeo.momentwo.feature.photo.impl.photolist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.lifecycle.compose.LifecycleStartEffect
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.ui.rememberPagingScreenState
import cord.eoeo.momentwo.core.data.toUiError
import kotlinx.coroutines.flow.emptyFlow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import cord.eoeo.momentwo.core.model.PhotoItem
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoListRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    navigateToPhotoDetail: (Int, Int, String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: PhotoListViewModel = hiltViewModel(),
) {
    val uiState: PhotoListState by viewModel.uiState.collectAsStateWithLifecycle()
    val refreshState: PullToRefreshState = rememberPullToRefreshState()
    LifecycleStartEffect(viewModel) { viewModel.initialize(); onStopOrDispose { } }
    val source = (uiState.source as? ScreenState.Success)?.content ?: remember { emptyFlow() }
    val photoPagingData = source.collectAsLazyPagingItems()
    val pagingState = rememberPagingScreenState(photoPagingData, Throwable::toUiError)
    val screenState = when (val ready = uiState.source) {
        ScreenState.Loading -> ScreenState.Loading
        is ScreenState.Error -> ScreenState.Error(ready.error)
        is ScreenState.Success -> pagingState
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri ->
            viewModel.onEvent(
                PhotoListEvent.OnUploadImage(imageUri)
            )
        }
    }

    PhotoListScreen(
        coroutineScope = coroutineScope,
        imageLoader = imageLoader,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.onEvent(event) },
        refreshState = { refreshState },
        photoPagingData = { photoPagingData },
        pagingState = { screenState },
        retrySource = { viewModel.initialize() },
        launchGallery = { galleryLauncher.launch("image/*") },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
        navigateToPhotoDetail = { albumId, photoId, url, liked -> viewModel.onEvent(PhotoListEvent.OnPhotoVisited); navigateToPhotoDetail(albumId, photoId, url, liked) }
    )
}
