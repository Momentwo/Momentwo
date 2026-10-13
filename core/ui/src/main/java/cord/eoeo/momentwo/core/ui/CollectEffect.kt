package cord.eoeo.momentwo.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/** Handler는 즉시 명령을 처리하고 Snackbar/애니메이션은 이 STARTED scope의 자식으로 실행한다. */
@Composable
fun <Effect> CollectEffect(flow: Flow<Effect>, handler: CoroutineScope.(Effect) -> Unit) {
    val owner = LocalLifecycleOwner.current
    val currentHandler by rememberUpdatedState(handler)
    LaunchedEffect(flow, owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { currentHandler(it) }
        }
    }
}
