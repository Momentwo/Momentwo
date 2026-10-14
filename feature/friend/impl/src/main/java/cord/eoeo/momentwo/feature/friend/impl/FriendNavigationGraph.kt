package cord.eoeo.momentwo.feature.friend.impl

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.feature.friend.api.FriendDestination
import kotlinx.coroutines.CoroutineScope

fun NavGraphBuilder.friendScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    accountGuard: @Composable (@Composable () -> Unit) -> Unit = { content -> content() },
) {
    composable<FriendDestination> {
        accountGuard { FriendRoute(coroutineScope = coroutineScope, imageLoader = imageLoader, popBackStack = popBackStack) }
    }
}
