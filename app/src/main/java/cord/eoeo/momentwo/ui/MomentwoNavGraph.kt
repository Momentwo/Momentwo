package cord.eoeo.momentwo.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import coil.ImageLoader
import cord.eoeo.momentwo.feature.album.impl.albumScreens
import cord.eoeo.momentwo.feature.album.impl.local.localLibraryScreen
import cord.eoeo.momentwo.feature.album.api.LocalLibraryDestination
import cord.eoeo.momentwo.feature.albumdetail.impl.albumDetailScreen
import cord.eoeo.momentwo.feature.auth.api.LoginDestination
import cord.eoeo.momentwo.feature.auth.impl.authScreens
import cord.eoeo.momentwo.feature.friend.impl.friendScreen
import cord.eoeo.momentwo.feature.photo.impl.photoScreens
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun MomentwoNavGraph(
    coroutineScope: CoroutineScope,
    navController: NavHostController,
    navActions: MomentwoNavigationActions,
    imageLoader: ImageLoader,
    insetsController: WindowInsetsControllerCompat,
) {
    NavHost(
        navController = navController,
        startDestination = LocalLibraryDestination,
        modifier = Modifier,
    ) {
        localLibraryScreen(navigateToLogin = { navController.navigate(LoginDestination) })
        friendScreen(
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            popBackStack = navActions.popBackStack,
        )

        albumDetailScreen(
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            popBackStack = navActions.popBackStack,
            navigateToPhotoList = navActions.navigateToPhotoList,
            subAlbumContent = { state, actions -> SubAlbumContentAdapter(imageLoader, state, actions) },
        )

        albumScreens(
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            popBackStack = navActions.popBackStack,
            navigateToCreateAlbum = navActions.navigateToCreateAlbum,
            navigateToProfile = { navActions.navigateToProfile(null) },
            navigateToFriend = navActions.navigateToFriend,
            navigateToAlbumDetail = navActions.navigateToAlbumDetail,
        )

        photoScreens(
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            insetsController = insetsController,
            popBackStack = navActions.popBackStack,
            navigateToPhotoDetail = navActions.navigateToPhotoDetail,
        )

        authScreens(
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            popBackStack = navActions.popBackStack,
            navigateToAlbum = navActions.navigateToAlbum,
            navigateToSignUp = navActions.navigateToSignUp,
            navigateToLogin = navActions.navigateToLogin,
        )
    }
}
