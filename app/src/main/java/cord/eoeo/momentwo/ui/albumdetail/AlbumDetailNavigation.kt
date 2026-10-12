package cord.eoeo.momentwo.ui.albumdetail

import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import cord.eoeo.momentwo.feature.subalbum.api.SubAlbumListDestination

sealed interface AlbumDetailDestination {
    @Serializable
    data object Member : AlbumDetailDestination

    @Serializable
    data object AlbumSetting : AlbumDetailDestination

    @Serializable
    data object ChangeImage : AlbumDetailDestination
}

class AlbumDetailNavigationActions(
    navController: NavHostController,
) {
    val popBackStack: () -> Unit = {
        navController.popBackStack()
    }
    val navigateToSubAlbumList: () -> Unit = {
        navController.navigate(SubAlbumListDestination) {
            launchSingleTop = true
        }
    }
    val navigateToMember: () -> Unit = {
        navController.navigate(AlbumDetailDestination.Member) {
            launchSingleTop = true
        }
    }
    val navigateToAlbumSetting: () -> Unit = {
        navController.navigate(AlbumDetailDestination.AlbumSetting) {
            launchSingleTop = true
        }
    }
    val navigateToChangeImage: () -> Unit = {
        navController.navigate(AlbumDetailDestination.ChangeImage) {
            launchSingleTop = true
        }
    }
}
