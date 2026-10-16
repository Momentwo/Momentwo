package cord.eoeo.momentwo.feature.auth.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil3.ImageLoader
import cord.eoeo.momentwo.feature.auth.api.LoginDestination
import cord.eoeo.momentwo.feature.auth.api.ProfileDestination
import cord.eoeo.momentwo.feature.auth.api.SignUpDestination
import cord.eoeo.momentwo.feature.auth.impl.login.LoginRoute
import cord.eoeo.momentwo.feature.auth.impl.profile.ProfileRoute
import cord.eoeo.momentwo.feature.auth.impl.signup.SignUpRoute
import kotlinx.coroutines.CoroutineScope

fun NavGraphBuilder.authScreens(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    navigateToAlbum: () -> Unit,
    navigateToSignUp: () -> Unit,
    navigateToLogin: () -> Unit,
) {
    composable<LoginDestination> {
        LoginRoute(coroutineScope, navigateToAlbum, navigateToSignUp)
    }
    composable<SignUpDestination> {
        SignUpRoute(coroutineScope, popBackStack, navigateToLogin)
    }
    composable<ProfileDestination> {
        ProfileRoute(coroutineScope, imageLoader, popBackStack)
    }
}
