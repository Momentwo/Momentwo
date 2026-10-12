package cord.eoeo.momentwo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader

@Composable
internal fun MomentwoApp(
    imageLoader: ImageLoader,
    insetsController: WindowInsetsControllerCompat,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val navActions = remember(navController) { MomentwoNavigationActions(navController) }

    MomentwoNavGraph(
        coroutineScope = coroutineScope,
        navController = navController,
        navActions = navActions,
        imageLoader = imageLoader,
        insetsController = insetsController,
    )
}
