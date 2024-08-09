package cord.eoeo.momentwo.ui.friend

import androidx.navigation.NavHostController

object FriendDestination {
    const val LIST_ROUTE = "list"
    const val REQUEST_ROUTE = "request"
}

class FriendNavigationActions(
    navController: NavHostController,
) {
    val navigateToFriendList: () -> Unit = {
        navController.navigate(FriendDestination.LIST_ROUTE) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
    val navigateToFriendRequest: () -> Unit = {
        navController.navigate(FriendDestination.REQUEST_ROUTE) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
}
