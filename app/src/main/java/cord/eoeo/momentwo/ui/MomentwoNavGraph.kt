package cord.eoeo.momentwo.ui
import cord.eoeo.momentwo.core.navigation.MomentwoDestination
import cord.eoeo.momentwo.feature.auth.api.LoginDestination
import cord.eoeo.momentwo.feature.friend.impl.friendScreen
import cord.eoeo.momentwo.feature.auth.impl.authScreens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.ui.album.AlbumRoute
import cord.eoeo.momentwo.ui.albumdetail.AlbumDetailRoute
import cord.eoeo.momentwo.ui.createalbum.CreateAlbumRoute
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

        authScreens(
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            popBackStack = navActions.popBackStack,
            navigateToAlbum = navActions.navigateToAlbum,
            navigateToSignUp = navActions.navigateToSignUp,
            navigateToLogin = navActions.navigateToLogin,
        )

        composable<MomentwoDestination.Album> {
            AlbumRoute(
                coroutineScope = coroutineScope,
                navigateToCreateAlbum = navActions.navigateToCreateAlbum,
                navigateToFriend = navActions.navigateToFriend,
                navigateToProfile = { navActions.navigateToProfile(null) },
                navigateToAlbumDetail = navActions.navigateToAlbumDetail,
                imageLoader = imageLoader,
            )
        }

        composable<MomentwoDestination.AlbumDetail> {
            AlbumDetailRoute(
                coroutineScope = coroutineScope,
                imageLoader = imageLoader,
                popBackStack = navActions.popBackStack,
                navigateToPhotoList = navActions.navigateToPhotoList,
                subAlbumContent = { state, actions -> SubAlbumContentAdapter(imageLoader, state, actions) },
            )
        }

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

        composable<MomentwoDestination.CreateAlbum> {
            CreateAlbumRoute(
                coroutineScope = coroutineScope,
                popBackStack = navActions.popBackStack,
            )
        }

    }
}
