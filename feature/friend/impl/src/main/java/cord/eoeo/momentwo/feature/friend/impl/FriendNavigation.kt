package cord.eoeo.momentwo.feature.friend.impl

import androidx.navigation.NavHostController

object FriendTabDestination {
    const val LIST_ROUTE = "list"
    const val REQUEST_ROUTE = "request"
}

class FriendNavigationActions(
    navController: NavHostController,
) {
    val navigateToFriendList: () -> Unit = {
        navController.navigate(FriendTabDestination.LIST_ROUTE) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
    val navigateToFriendRequest: () -> Unit = {
        navController.navigate(FriendTabDestination.REQUEST_ROUTE) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
}
