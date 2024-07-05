package cord.eoeo.momentwo.ui

import androidx.navigation.NavHostController

object MomentwoDestination {
    const val LOGIN_ROUTE = "login"
    const val SIGNUP_ROUTE = "signup"
    const val ALBUM_ROUTE = "album"
}

class MomentwoNavigationActions(navController: NavHostController) {
    val popBackStack: () -> Unit = {
        navController.popBackStack()
    }
    val navigateToLogin: () -> Unit = {
        navController.navigate(MomentwoDestination.LOGIN_ROUTE) {
            launchSingleTop = true
            popUpTo(MomentwoDestination.LOGIN_ROUTE)
        }
    }
    val navigateToSignUp: () -> Unit = {
        navController.navigate(MomentwoDestination.SIGNUP_ROUTE)
    }
    val navigateToAlbum: () -> Unit = {
        navController.navigate(MomentwoDestination.ALBUM_ROUTE)
    }
}
