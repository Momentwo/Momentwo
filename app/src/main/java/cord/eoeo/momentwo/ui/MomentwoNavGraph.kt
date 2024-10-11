package cord.eoeo.momentwo.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.ui.album.AlbumRoute
import cord.eoeo.momentwo.ui.albumdetail.AlbumDetailRoute
import cord.eoeo.momentwo.ui.createalbum.CreateAlbumRoute
import cord.eoeo.momentwo.ui.friend.FriendRoute
import cord.eoeo.momentwo.ui.login.LoginRoute
import cord.eoeo.momentwo.ui.photolist.PhotoListRoute
import cord.eoeo.momentwo.ui.signup.SignUpRoute
import kotlinx.coroutines.CoroutineScope

@Composable
fun MomentwoNavGraph(
    coroutineScope: CoroutineScope,
    navController: NavHostController,
    navActions: MomentwoNavigationActions,
    imageLoader: ImageLoader,
) {
    NavHost(
        navController = navController,
        startDestination = MomentwoDestination.Login,
        modifier = Modifier,
    ) {
        composable<MomentwoDestination.Login> {
            LoginRoute(
                coroutineScope = coroutineScope,
                navigateToAlbum = navActions.navigateToAlbum,
                navigateToSignUp = navActions.navigateToSignUp,
            )
        }

        composable<MomentwoDestination.SignUp> {
            SignUpRoute(
                coroutineScope = coroutineScope,
                popBackStack = navActions.popBackStack,
                navigateToLogin = navActions.navigateToLogin,
            )
        }

        composable<MomentwoDestination.Album> {
            AlbumRoute(
                coroutineScope = coroutineScope,
                navigateToCreateAlbum = navActions.navigateToCreateAlbum,
                navigateToFriend = navActions.navigateToFriend,
                navigateToAlbumDetail = navActions.navigateToAlbumDetail,
                imageLoader = imageLoader,
            )
        }

        composable<MomentwoDestination.AlbumDetail> {
            AlbumDetailRoute(
                coroutineScope = coroutineScope,
                imageLoader = imageLoader,
                popBackStack = navActions.popBackStack,
                navigateToPhotoList = navActions.navigeteToPhotoList
            )
        }

        composable<MomentwoDestination.PhotoList> {
            PhotoListRoute(
                coroutineScope = coroutineScope,
                imageLoader = imageLoader,
                popBackStack = navActions.popBackStack,
            )
        }

        composable<MomentwoDestination.CreateAlbum> {
            CreateAlbumRoute(
                coroutineScope = coroutineScope,
                popBackStack = navActions.popBackStack,
            )
        }

        composable<MomentwoDestination.Friend> {
            FriendRoute(
                coroutineScope = coroutineScope,
                popBackStack = navActions.popBackStack,
            )
        }
    }
}
