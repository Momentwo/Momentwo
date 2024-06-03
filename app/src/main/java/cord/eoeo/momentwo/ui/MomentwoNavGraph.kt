package cord.eoeo.momentwo.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cord.eoeo.momentwo.ui.login.LoginRoute
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
        modifier = Modifier
    ) {
        composable(
            route = MomentwoDestination.LOGIN_ROUTE
        ) {
            LoginRoute(
                coroutineScope = coroutineScope,
                navigateToAlbum = navActions.navigateToAlbum
            )
        }

        composable(
            route = MomentwoDestination.ALBUM_ROUTE
        ) {

        }
    }
}