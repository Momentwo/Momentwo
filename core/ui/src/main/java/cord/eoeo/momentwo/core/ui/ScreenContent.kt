package cord.eoeo.momentwo.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cord.eoeo.momentwo.core.common.RefreshState
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.common.UiError

@Composable
fun UiError.message(): String = stringResource(when (this) {
    UiError.Network -> R.string.error_network
    UiError.Server -> R.string.error_server
    UiError.Unauthorized -> R.string.error_unauthorized
    UiError.Forbidden -> R.string.error_forbidden
    UiError.NotFound -> R.string.error_not_found
    UiError.Unknown -> R.string.error_unknown
})

@Composable
fun <T : Any> ScreenContent(
    state: ScreenState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        ScreenState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is ScreenState.Error -> Column(
            modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(state.error.message())
            if (state.error.canRetry) TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
        is ScreenState.Success -> Column(modifier.fillMaxSize()) {
            when (val refresh = state.refresh) {
                RefreshState.Idle -> Unit
                RefreshState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                is RefreshState.Error -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(refresh.error.message(), Modifier.weight(1f).padding(horizontal = 8.dp))
                    if (refresh.error.canRetry) TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
            }
            Box(Modifier.weight(1f)) { content(state.content) }
        }
    }
}

@Composable
fun EmptyContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.empty_content)) }
}
