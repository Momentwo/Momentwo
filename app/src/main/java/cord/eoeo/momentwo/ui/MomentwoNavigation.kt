package cord.eoeo.momentwo.ui

import androidx.navigation.NavHostController

object MomentwoDestination {
    const val LOGIN_ROUTE = "login"
    const val ALBUM_ROUTE = "album"
}

class MomentwoNavigationActions(navController: NavHostController) {
    val navigateToLogin: () -> Unit = {
        navController.navigate(MomentwoDestination.LOGIN_ROUTE)
    }
    val navigateToAlbum: () -> Unit = {
        navController.navigate(MomentwoDestination.ALBUM_ROUTE)
    }
}
