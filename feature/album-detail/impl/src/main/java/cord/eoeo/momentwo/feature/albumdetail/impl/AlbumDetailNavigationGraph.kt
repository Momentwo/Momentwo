package cord.eoeo.momentwo.feature.albumdetail.impl

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.feature.albumdetail.api.AlbumDetailDestination
import kotlinx.coroutines.CoroutineScope

fun NavGraphBuilder.albumDetailScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    navigateToPhotoList: (Int, Int, String, String) -> Unit,
    subAlbumContent: @Composable (SubAlbumContentState, SubAlbumContentActions) -> Unit,
) {
    composable<AlbumDetailDestination> {
        AlbumDetailRoute(coroutineScope, imageLoader, popBackStack, navigateToPhotoList, subAlbumContent)
    }
}
