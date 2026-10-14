package cord.eoeo.momentwo.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import coil.ImageLoader
import cord.eoeo.momentwo.feature.album.impl.albumScreens
import cord.eoeo.momentwo.feature.album.impl.account.accountLibraryScreen
import cord.eoeo.momentwo.feature.album.impl.account.AccountAccessGuard
import cord.eoeo.momentwo.feature.album.api.AccountLibraryDestination
import cord.eoeo.momentwo.feature.album.impl.local.localLibraryScreen
import cord.eoeo.momentwo.feature.albumdetail.api.LocalAlbumDetailDestination
import cord.eoeo.momentwo.feature.albumdetail.api.AlbumDetailDestination
import cord.eoeo.momentwo.feature.albumdetail.impl.local.localAlbumDetailScreen
import cord.eoeo.momentwo.feature.album.api.LocalLibraryDestination
import cord.eoeo.momentwo.feature.albumdetail.impl.albumDetailScreen
import cord.eoeo.momentwo.feature.auth.api.LoginDestination
import cord.eoeo.momentwo.feature.auth.impl.authScreens
import cord.eoeo.momentwo.feature.friend.impl.friendScreen
import cord.eoeo.momentwo.feature.photo.impl.photoScreens
import cord.eoeo.momentwo.feature.photo.impl.local.localPhotoScreens
import cord.eoeo.momentwo.feature.photo.api.LocalPhotoListDestination
import cord.eoeo.momentwo.feature.photo.api.LocalPhotoDetailDestination
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun MomentwoNavGraph(
    coroutineScope: CoroutineScope,
    navController: NavHostController,
    navActions: MomentwoNavigationActions,
    imageLoader: ImageLoader,
    insetsController: WindowInsetsControllerCompat,
) {
    NavHost(
        navController = navController,
        startDestination = LocalLibraryDestination,
        modifier = Modifier,
    ) {
        localLibraryScreen(navigateToLogin = { navController.navigate(AccountLibraryDestination) }, navigateToAlbum = { navController.navigate(LocalAlbumDetailDestination(it)) })
        localAlbumDetailScreen(imageLoader, navActions.popBackStack, navigateToLogin = { navController.navigate(AccountLibraryDestination) }, openPhotos = { navController.navigate(LocalPhotoListDestination(it)) }, openAccountPhotos = navActions.navigateToPhotoList, openSharedAlbum = {id,title,subtitle,cover -> navController.navigate(AlbumDetailDestination(id,title,subtitle,cover)) })
        localPhotoScreens(imageLoader,insetsController,navActions.popBackStack,openPhoto = { navController.navigate(LocalPhotoDetailDestination(it)) })
        accountLibraryScreen(navActions.popBackStack, { navController.navigate(LoginDestination) }, navActions.navigateToFriend, { navController.navigate(LocalAlbumDetailDestination(it)) })
        friendScreen(
            accountGuard = { content -> AccountAccessGuard({ navController.navigate(AccountLibraryDestination) { launchSingleTop = true; popUpTo<LocalLibraryDestination>() } },content) },
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            popBackStack = navActions.popBackStack,
        )

        albumDetailScreen(
            accountGuard = { content -> AccountAccessGuard({ navController.navigate(AccountLibraryDestination) { launchSingleTop = true; popUpTo<LocalLibraryDestination>() } },content) },
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            popBackStack = navActions.popBackStack,
            navigateToPhotoList = navActions.navigateToPhotoList,
            subAlbumContent = { state, actions -> SubAlbumContentAdapter(imageLoader, state, actions) },
        )

        albumScreens(
            accountGuard = { content -> AccountAccessGuard({ navController.navigate(AccountLibraryDestination) { launchSingleTop = true; popUpTo<LocalLibraryDestination>() } },content) },
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            popBackStack = navActions.popBackStack,
            navigateToCreateAlbum = navActions.navigateToCreateAlbum,
            navigateToProfile = { navActions.navigateToProfile(null) },
            navigateToFriend = navActions.navigateToFriend,
            navigateToAlbumDetail = navActions.navigateToAlbumDetail,
        )

        photoScreens(
            accountGuard = { content -> AccountAccessGuard({ navController.navigate(AccountLibraryDestination) { launchSingleTop = true; popUpTo<LocalLibraryDestination>() } },content) },
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            insetsController = insetsController,
            popBackStack = navActions.popBackStack,
            navigateToPhotoDetail = navActions.navigateToPhotoDetail,
        )

        authScreens(
            coroutineScope = coroutineScope,
            imageLoader = imageLoader,
            popBackStack = navActions.popBackStack,
            navigateToAlbum = { navController.navigate(AccountLibraryDestination) { launchSingleTop = true; popUpTo<LocalLibraryDestination>() } },
            navigateToSignUp = navActions.navigateToSignUp,
            navigateToLogin = navActions.navigateToLogin,
        )
    }
}
