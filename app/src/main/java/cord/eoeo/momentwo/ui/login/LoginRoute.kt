package cord.eoeo.momentwo.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

@Composable
fun LoginRoute(
    coroutineScope: CoroutineScope,
    navigateToAlbum: () -> Unit,
    navigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    LoginScreen(
        coroutineScope = coroutineScope,
        navigateToAlbum = navigateToAlbum,
        navigateToSignUp = navigateToSignUp,
    )
}
