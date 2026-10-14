package cord.eoeo.momentwo.feature.photo.impl.presentation
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.ui.unit.dp
import cord.eoeo.momentwo.core.common.RefreshState
import cord.eoeo.momentwo.core.ui.message
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
    val refresh=(screen as? ScreenState.Success)?.refresh ?: RefreshState.Idle
    val bodyState=if(screen is ScreenState.Success)screen.copy(refresh=RefreshState.Idle)else screen
    Scaffold(topBar={AnimatedVisibility(barsVisible,enter=fadeIn(),exit=fadeOut()){PhotoDetailTopAppBar(back) {
        when(val status=refresh) {
            RefreshState.Idle -> Unit
            RefreshState.Loading -> CircularProgressIndicator(Modifier.size(18.dp),strokeWidth=2.dp)
            is RefreshState.Error -> IconButton(onClick=retry,enabled=status.error.canRetry){Icon(Icons.Default.CloudOff,status.error.message())}
        }
        actions()
    }}},
        bottomBar={AnimatedVisibility(menuVisible && screen is ScreenState.Success,enter=fadeIn(),exit=fadeOut()){footer()}},
        snackbarHost={SnackbarHost(messages)},modifier=Modifier.fillMaxSize()) {
        ScreenContent(bodyState,retry) {
            AsyncImage(image,"사진",loader,contentScale=ContentScale.Fit,modifier=Modifier.fillMaxSize().drawBehind{drawRect(color)}.clickable(indication=null,interactionSource=null,onClick=toggle))
        }
    }
}
