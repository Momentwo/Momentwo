package cord.eoeo.momentwo.ui.signup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SignUpRoute(
    coroutineScope: CoroutineScope,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    SignUpScreen(
        coroutineScope = coroutineScope,
        pagerState = { pagerState },
        navigateToLogin = navigateToLogin,
    )
}
