package cord.eoeo.momentwo.ui

import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

sealed interface MomentwoDestination {
    @Serializable
    data object Login : MomentwoDestination

    @Serializable
    data object SignUp : MomentwoDestination

    @Serializable
    data object Album : MomentwoDestination

    @Serializable
    data object AlbumDetail : MomentwoDestination

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
    val navigateToAlbumDetail: () -> Unit = {
        navController.navigate(MomentwoDestination.AlbumDetail)
    }
    val navigateToCreateAlbum: () -> Unit = {
        navController.navigate(MomentwoDestination.CreateAlbum)
    }
    val navigateToFriend: () -> Unit = {
        navController.navigate(MomentwoDestination.Friend)
    }
}
