package cord.eoeo.momentwo.feature.albumdetail.impl

import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import cord.eoeo.momentwo.feature.subalbum.api.SubAlbumListDestination

internal sealed interface AlbumDetailTabDestination {
    @Serializable
    @SerialName("cord.eoeo.momentwo.ui.albumdetail.AlbumDetailDestination.Member")
    data object Member : AlbumDetailTabDestination

    @Serializable
    @SerialName("cord.eoeo.momentwo.ui.albumdetail.AlbumDetailDestination.AlbumSetting")
    data object AlbumSetting : AlbumDetailTabDestination

    @Serializable
    @SerialName("cord.eoeo.momentwo.ui.albumdetail.AlbumDetailDestination.ChangeImage")
    data object ChangeImage : AlbumDetailTabDestination
}

internal class AlbumDetailNavigationActions(
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
        navController.navigate(AlbumDetailTabDestination.Member) {
            launchSingleTop = true
        }
    }
    val navigateToAlbumSetting: () -> Unit = {
        navController.navigate(AlbumDetailTabDestination.AlbumSetting) {
            launchSingleTop = true
        }
    }
    val navigateToChangeImage: () -> Unit = {
        navController.navigate(AlbumDetailTabDestination.ChangeImage) {
            launchSingleTop = true
        }
    }
}
