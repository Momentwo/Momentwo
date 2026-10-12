package cord.eoeo.momentwo.ui
import cord.eoeo.momentwo.core.navigation.MomentwoDestination
import cord.eoeo.momentwo.feature.auth.api.LoginDestination
import cord.eoeo.momentwo.feature.friend.impl.friendScreen
import cord.eoeo.momentwo.feature.albumdetail.impl.albumDetailScreen
import cord.eoeo.momentwo.feature.album.impl.albumScreens
import cord.eoeo.momentwo.feature.auth.impl.authScreens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.ui.photodetail.PhotoDetailRoute
import cord.eoeo.momentwo.ui.photolist.PhotoListRoute
import kotlinx.coroutines.CoroutineScope

@Composable
fun MomentwoNavGraph(
    coroutineScope: CoroutineScope,
    navController: NavHostController,
    navActions: MomentwoNavigationActions,
    imageLoader: ImageLoader,
    insetsController: WindowInsetsControllerCompat,
) {
    NavHost(
        navController = navController,
        startDestination = LoginDestination,
        modifier = Modifier,
    ) {
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

        authScreens(
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            popBackStack = navActions.popBackStack,
            navigateToAlbum = navActions.navigateToAlbum,
            navigateToSignUp = navActions.navigateToSignUp,
            navigateToLogin = navActions.navigateToLogin,
        )

        composable<MomentwoDestination.PhotoList> {
            PhotoListRoute(
                coroutineScope = coroutineScope,
                imageLoader = imageLoader,
                popBackStack = navActions.popBackStack,
                navigateToPhotoDetail = navActions.navigateToPhotoDetail,
            )
        }

        composable<MomentwoDestination.PhotoDetail> {
            PhotoDetailRoute(
                coroutineScope = coroutineScope,
                imageLoader = imageLoader,
                insetsController = insetsController,
                popBackStack = navActions.popBackStack,
            )
        }

    }
}
