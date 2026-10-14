package cord.eoeo.momentwo.feature.photo.impl.presentation
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.ImageLoader
import coil.compose.AsyncImage
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.feature.photo.impl.photodetail.PhotoDetailTopAppBar
@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun <T: Any> PhotoViewer(loader: ImageLoader, image: Any?, screen: ScreenState<T>, color: Color,
    barsVisible: Boolean, menuVisible: Boolean, messages: SnackbarHostState, back: () -> Unit, toggle: () -> Unit, retry: () -> Unit,
    actions: @Composable () -> Unit = {}, footer: @Composable () -> Unit) {
    Scaffold(topBar={AnimatedVisibility(barsVisible,enter=fadeIn(),exit=fadeOut()){PhotoDetailTopAppBar(back,actions)}},
        bottomBar={AnimatedVisibility(menuVisible && screen is ScreenState.Success,enter=fadeIn(),exit=fadeOut()){footer()}},
        snackbarHost={SnackbarHost(messages)},modifier=Modifier.fillMaxSize()) {
        ScreenContent(screen,retry) {
            AsyncImage(image,"사진",loader,contentScale=ContentScale.Fit,modifier=Modifier.fillMaxSize().drawBehind{drawRect(color)}.clickable(indication=null,interactionSource=null,onClick=toggle))
        }
    }
}
