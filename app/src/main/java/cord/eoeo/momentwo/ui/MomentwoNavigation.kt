package cord.eoeo.momentwo.ui

import androidx.navigation.NavHostController
import cord.eoeo.momentwo.ui.model.AlbumItem
import kotlinx.serialization.Serializable

sealed interface MomentwoDestination {
    @Serializable
    data object Login : MomentwoDestination

    @Serializable
    data object SignUp : MomentwoDestination

    @Serializable
    data object Album : MomentwoDestination

    @Serializable
    data class AlbumDetail(
        val id: Int,
        val title: String,
        val subTitle: String,
        val imageUrl: String,
    )

    @Serializable
    data class PhotoList(
        val albumId: Int,
        val subAlbumId: Int,
        val albumTitle: String,
        val subAlbumTitle: String,
    ) : MomentwoDestination

    @Serializable
    data object CreateAlbum : MomentwoDestination

    @Serializable
    data object Friend : MomentwoDestination
}

class MomentwoNavigationActions(navController: NavHostController) {
    val popBackStack: () -> Unit = {
        navController.popBackStack()
    }
    val navigateToLogin: () -> Unit = {
        navController.navigate(MomentwoDestination.Login) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
    val navigateToSignUp: () -> Unit = {
        navController.navigate(MomentwoDestination.SignUp)
    }
    val navigateToAlbum: () -> Unit = {
        navController.navigate(MomentwoDestination.Album) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
    val navigateToAlbumDetail: (AlbumItem) -> Unit = { (id, title, subTitle, imageUrl) ->
        navController.navigate(MomentwoDestination.AlbumDetail(id, title, subTitle, imageUrl))
    }
    val navigeteToPhotoList: (Int, Int, String, String) -> Unit = { albumId, subAlbumId, albumTitle, subAlbumTitle ->
        navController.navigate(MomentwoDestination.PhotoList(albumId, subAlbumId, albumTitle, subAlbumTitle))
    }
    val navigateToCreateAlbum: () -> Unit = {
        navController.navigate(MomentwoDestination.CreateAlbum)
    }
    val navigateToFriend: () -> Unit = {
        navController.navigate(MomentwoDestination.Friend)
    }
}
