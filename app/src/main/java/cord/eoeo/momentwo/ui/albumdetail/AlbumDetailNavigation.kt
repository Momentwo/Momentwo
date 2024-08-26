package cord.eoeo.momentwo.ui.albumdetail

import androidx.navigation.NavHostController

object AlbumDetailDestination {
    const val SUB_ALBUM_LIST_ROUTE = "sub_album_list"
    const val MEMBER_ROUTE = "member"
    const val ALBUM_SETTING_ROUTE = "album_setting"
}

class AlbumDetailNavigationActions(
    navController: NavHostController,
) {
    val navigateToSubAlbumList: () -> Unit = {
        navController.navigate(AlbumDetailDestination.SUB_ALBUM_LIST_ROUTE) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
    val navigateToMember: () -> Unit = {
        navController.navigate(AlbumDetailDestination.MEMBER_ROUTE) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
    val navigateToAlbumSetting: () -> Unit = {
        navController.navigate(AlbumDetailDestination.ALBUM_SETTING_ROUTE) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
}
