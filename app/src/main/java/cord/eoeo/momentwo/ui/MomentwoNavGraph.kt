package cord.eoeo.momentwo.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.ui.album.AlbumRoute
import cord.eoeo.momentwo.ui.createalbum.CreateAlbumRoute
import cord.eoeo.momentwo.ui.friend.FriendRoute
import cord.eoeo.momentwo.ui.login.LoginRoute
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
        startDestination = MomentwoDestination.LOGIN_ROUTE,
        modifier = Modifier,
    ) {
        composable(
            route = MomentwoDestination.LOGIN_ROUTE,
        ) {
            LoginRoute(
                coroutineScope = coroutineScope,
                navigateToAlbum = navActions.navigateToAlbum,
                navigateToSignUp = navActions.navigateToSignUp,
            )
        }

        composable(
            route = MomentwoDestination.SIGNUP_ROUTE,
        ) {
            SignUpRoute(
                coroutineScope = coroutineScope,
                popBackStack = navActions.popBackStack,
                navigateToLogin = navActions.navigateToLogin,
            )
        }

        composable(
            route = MomentwoDestination.ALBUM_ROUTE,
        ) {
            AlbumRoute(
                coroutineScope = coroutineScope,
                navigateToCreateAlbum = navActions.navigateToCreateAlbum,
                navigateToFriend = navActions.navigateToFriend,
                imageLoader = imageLoader,
            )
        }

        composable(
            route = MomentwoDestination.CREATE_ALBUM_ROUTE,
        ) {
            CreateAlbumRoute(
                coroutineScope = coroutineScope,
                popBackStack = navActions.popBackStack,
            )
        }

        composable(
            route = MomentwoDestination.FRIEND_ROUTE,
        ) {
            FriendRoute(
                coroutineScope = coroutineScope,
                popBackStack = navActions.popBackStack,
            )
        }
    }
}
