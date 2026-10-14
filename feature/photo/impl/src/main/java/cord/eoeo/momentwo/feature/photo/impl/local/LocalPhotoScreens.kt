package cord.eoeo.momentwo.feature.photo.impl.local
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.core.view.WindowInsetsControllerCompat
import coil.ImageLoader
import cord.eoeo.momentwo.feature.photo.api.*
fun NavGraphBuilder.localPhotoScreens(imageLoader: ImageLoader, controller: WindowInsetsControllerCompat, popBackStack: () -> Unit, openPhoto: (String) -> Unit) {
    composable<LocalPhotoListDestination> { LocalPhotoListRoute(imageLoader,popBackStack,openPhoto) }
    composable<LocalPhotoDetailDestination> { LocalPhotoRoute(imageLoader,controller,popBackStack) }
}
