package cord.eoeo.momentwo.ui.photodetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import cord.eoeo.momentwo.core.model.CommentItem
import cord.eoeo.momentwo.core.designsystem.theme.backgroundDark
import kotlinx.coroutines.CoroutineScope

@Composable
fun PhotoDetailRoute(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    insetsController: WindowInsetsControllerCompat,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: PhotoDetailViewModel = hiltViewModel(),
) {
    val uiState: PhotoDetailContract.State by viewModel.uiState.collectAsStateWithLifecycle()
    val commentPagingData: LazyPagingItems<CommentItem> = uiState.commentPagingData.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState()
    val animatedBackgroundColor: Color by animateColorAsState(
        targetValue = if (uiState.isMenuVisible) MaterialTheme.colorScheme.background else backgroundDark,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "backgroundColor"
    )

    PhotoDetailScreen(
        coroutineScope = coroutineScope,
        imageLoader = imageLoader,
        insetsController = insetsController,
        uiState = { uiState },
        effectFlow = { viewModel.effect },
        onEvent = { event -> viewModel.setEvent(event) },
        commentPagingData = { commentPagingData },
        lazyListState = { lazyListState },
        animatedBackgroundColor = { animatedBackgroundColor },
        snackbarHostState = { snackbarHostState },
        popBackStack = popBackStack,
    )
}
