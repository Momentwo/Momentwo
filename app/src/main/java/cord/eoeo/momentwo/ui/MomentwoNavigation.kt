package cord.eoeo.momentwo.ui

import androidx.navigation.NavHostController
import cord.eoeo.momentwo.core.model.AlbumItem
import cord.eoeo.momentwo.feature.friend.api.FriendDestination
import cord.eoeo.momentwo.feature.albumdetail.api.AlbumDetailDestination
import cord.eoeo.momentwo.feature.album.api.AlbumDestination
import cord.eoeo.momentwo.feature.album.api.CreateAlbumDestination
import cord.eoeo.momentwo.feature.photo.api.PhotoListDestination
import cord.eoeo.momentwo.feature.photo.api.PhotoDetailDestination
import cord.eoeo.momentwo.core.navigation.MomentwoDestination
import cord.eoeo.momentwo.feature.auth.api.LoginDestination
import cord.eoeo.momentwo.feature.auth.api.SignUpDestination
import cord.eoeo.momentwo.feature.auth.api.ProfileDestination

class MomentwoNavigationActions(
    navController: NavHostController,
) {
    val popBackStack: () -> Unit = {
        navController.popBackStack()
    }
    val navigateToLogin: () -> Unit = {
        navController.navigate(LoginDestination) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
    val navigateToSignUp: () -> Unit = {
        navController.navigate(SignUpDestination)
    }
    val navigateToAlbum: () -> Unit = {
        navController.navigate(AlbumDestination) {
            launchSingleTop = true
            popUpTo(navController.graph.id)
        }
    }
    val navigateToAlbumDetail: (AlbumItem) -> Unit = { (id, title, subTitle, imageUrl) ->
        navController.navigate(AlbumDetailDestination(id, title, subTitle, imageUrl))
    }
    val navigateToPhotoList: (Int, Int, String, String) -> Unit = { albumId, subAlbumId, albumTitle, subAlbumTitle ->
        navController.navigate(PhotoListDestination(albumId, subAlbumId, albumTitle, subAlbumTitle))
    }
    val navigateToPhotoDetail: (Int, Int, String, Boolean) -> Unit = { albumId, photoId, photoUrl, isLiked ->
        navController.navigate(PhotoDetailDestination(albumId, photoId, photoUrl, isLiked))
    }
    val navigateToCreateAlbum: () -> Unit = {
        navController.navigate(CreateAlbumDestination)
    }
    val navigateToProfile: (String?) -> Unit = { nickname ->
        navController.navigate(ProfileDestination(nickname))
    }
    val navigateToFriend: () -> Unit = {
        navController.navigate(FriendDestination)
    }
}
