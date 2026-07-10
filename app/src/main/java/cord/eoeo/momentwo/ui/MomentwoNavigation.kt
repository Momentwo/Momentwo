package cord.eoeo.momentwo.ui

import androidx.navigation.NavHostController
import cord.eoeo.momentwo.core.model.AlbumItem
import cord.eoeo.momentwo.core.navigation.MomentwoDestination

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
