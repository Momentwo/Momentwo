package cord.eoeo.momentwo.ui

import androidx.navigation.NavHostController

object MomentwoDestination {
    const val LOGIN_ROUTE = "login"
    const val SIGNUP_ROUTE = "signup"
    const val ALBUM_ROUTE = "album"
    const val CREATE_ALBUM_ROUTE = "create_album"
    const val FRIEND_ROUTE = "friend"
}

class MomentwoNavigationActions(navController: NavHostController) {
    val popBackStack: () -> Unit = {
        navController.popBackStack()
    }
    val navigateToLogin: () -> Unit = {
        navController.navigate(MomentwoDestination.LOGIN_ROUTE) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
    val navigateToSignUp: () -> Unit = {
        navController.navigate(MomentwoDestination.SIGNUP_ROUTE)
    }
    val navigateToAlbum: () -> Unit = {
        navController.navigate(MomentwoDestination.ALBUM_ROUTE) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
    val navigateToCreateAlbum: () -> Unit = {
        navController.navigate(MomentwoDestination.CREATE_ALBUM_ROUTE)
    }
    val navigateToFriend: () -> Unit = {
        navController.navigate(MomentwoDestination.FRIEND_ROUTE)
    }
}
