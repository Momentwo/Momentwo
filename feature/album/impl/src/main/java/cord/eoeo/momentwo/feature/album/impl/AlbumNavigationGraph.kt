package cord.eoeo.momentwo.feature.album.impl

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.core.model.AlbumItem
import cord.eoeo.momentwo.feature.album.api.AlbumDestination
import cord.eoeo.momentwo.feature.album.api.CreateAlbumDestination
import cord.eoeo.momentwo.feature.album.impl.album.AlbumRoute
import cord.eoeo.momentwo.feature.album.impl.createalbum.CreateAlbumRoute
import kotlinx.coroutines.CoroutineScope

fun NavGraphBuilder.albumScreens(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    navigateToCreateAlbum: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToFriend: () -> Unit,
    navigateToAlbumDetail: (AlbumItem) -> Unit,
    device: () -> Unit = {},
    account: () -> Unit = {},
    settings: () -> Unit = {},
    accountGuard: @Composable (@Composable () -> Unit) -> Unit = { content -> content() },
) {
    composable<AlbumDestination> {
        accountGuard { AlbumRoute(coroutineScope, imageLoader, navigateToCreateAlbum, navigateToProfile, navigateToFriend, navigateToAlbumDetail, device, account, settings) }
    }
    composable<CreateAlbumDestination> {
        accountGuard { CreateAlbumRoute(coroutineScope, popBackStack) }
    }
}
