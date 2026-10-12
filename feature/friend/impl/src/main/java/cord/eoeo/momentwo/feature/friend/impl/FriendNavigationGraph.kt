package cord.eoeo.momentwo.feature.friend.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.feature.friend.api.FriendDestination
import kotlinx.coroutines.CoroutineScope

fun NavGraphBuilder.friendScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
) {
    composable<FriendDestination> {
        FriendRoute(coroutineScope = coroutineScope, imageLoader = imageLoader, popBackStack = popBackStack)
    }
}
