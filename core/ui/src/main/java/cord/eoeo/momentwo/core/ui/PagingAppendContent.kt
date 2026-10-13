package cord.eoeo.momentwo.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import cord.eoeo.momentwo.core.common.UiError

@Composable
fun PagingAppendContent(state: LoadState, errorMapper: (Throwable) -> UiError, onRetry: () -> Unit) {
    when (state) {
        is LoadState.Loading -> Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is LoadState.NotLoading -> Unit
        is LoadState.Error -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val error = errorMapper(state.error)
            Text(error.message(), Modifier.weight(1f).padding(8.dp))
            if (error.canRetry) TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}
