package cord.eoeo.momentwo.feature.photo.impl

import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.feature.photo.api.PhotoListDestination
import cord.eoeo.momentwo.feature.photo.api.PhotoDetailDestination
import cord.eoeo.momentwo.feature.photo.impl.photolist.PhotoListRoute
import cord.eoeo.momentwo.feature.photo.impl.photodetail.PhotoDetailRoute
import kotlinx.coroutines.CoroutineScope

fun NavGraphBuilder.photoScreens(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    insetsController: WindowInsetsControllerCompat,
    popBackStack: () -> Unit,
    navigateToPhotoDetail: (Int, Int, String, Boolean) -> Unit,
    accountGuard: @Composable (@Composable () -> Unit) -> Unit = { content -> content() },
) {
    composable<PhotoListDestination> {
        accountGuard { PhotoListRoute(coroutineScope, imageLoader, popBackStack, navigateToPhotoDetail) }
    }
    composable<PhotoDetailDestination> {
        accountGuard { PhotoDetailRoute(coroutineScope, imageLoader, insetsController, popBackStack) }
    }
}
