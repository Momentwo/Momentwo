package cord.eoeo.momentwo.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cord.eoeo.momentwo.ui.album.AlbumRoute
import cord.eoeo.momentwo.ui.createalbum.CreateAlbumRoute
import cord.eoeo.momentwo.ui.login.LoginRoute
import cord.eoeo.momentwo.ui.signup.SignUpRoute
import kotlinx.coroutines.CoroutineScope

@Composable
fun MomentwoNavGraph(
    coroutineScope: CoroutineScope,
    navController: NavHostController,
    navActions: MomentwoNavigationActions
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
    }
}
