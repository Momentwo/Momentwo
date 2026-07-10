package cord.eoeo.momentwo.ui

import androidx.navigation.NavHostController
import cord.eoeo.momentwo.core.model.AlbumItem
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
    ) : MomentwoDestination

    @Serializable
    data class PhotoList(
        val albumId: Int,
        val subAlbumId: Int,
        val albumTitle: String,
        val subAlbumTitle: String,
    ) : MomentwoDestination

    @Serializable
    data class PhotoDetail(
        val albumId: Int,
        val photoId: Int,
        val photoUrl: String,
        val isLiked: Boolean,
    ) : MomentwoDestination

    @Serializable
    data object CreateAlbum : MomentwoDestination

    @Serializable
    data class Profile(
        val nickname: String?,
    ) : MomentwoDestination

    @Serializable
    data object Friend : MomentwoDestination
}

class MomentwoNavigationActions(
    navController: NavHostController,
) {
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
    val navigateToPhotoList: (Int, Int, String, String) -> Unit = { albumId, subAlbumId, albumTitle, subAlbumTitle ->
        navController.navigate(MomentwoDestination.PhotoList(albumId, subAlbumId, albumTitle, subAlbumTitle))
    }
    val navigateToPhotoDetail: (Int, Int, String, Boolean) -> Unit = { albumId, photoId, photoUrl, isLiked ->
        navController.navigate(MomentwoDestination.PhotoDetail(albumId, photoId, photoUrl, isLiked))
    }
    val navigateToCreateAlbum: () -> Unit = {
        navController.navigate(MomentwoDestination.CreateAlbum)
    }
    val navigateToProfile: (String?) -> Unit = { nickname ->
        navController.navigate(MomentwoDestination.Profile(nickname))
    }
    val navigateToFriend: () -> Unit = {
        navController.navigate(MomentwoDestination.Friend)
    }
}
