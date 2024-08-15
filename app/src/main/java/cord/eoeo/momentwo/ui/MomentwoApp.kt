package cord.eoeo.momentwo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader

@Composable
fun MomentwoApp(
    imageLoader: ImageLoader,
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
    )
}
