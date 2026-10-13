package cord.eoeo.momentwo.core.ui

import androidx.compose.runtime.*
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import cord.eoeo.momentwo.core.common.*

/** Paging을 상태 원본으로 사용한다. 최초 성공한 빈 결과도 refresh 중에는 유지한다. */
@Composable
fun <T : Any> rememberPagingScreenState(items: LazyPagingItems<T>, errorMapper: (Throwable) -> UiError): ScreenState<Unit> {
    var hasPresented by remember(items) { mutableStateOf(false) }
    val state = pagingScreenState(items.loadState, items.itemCount, hasPresented, errorMapper)
    SideEffect {
        if (state is ScreenState.Success && state.refresh == RefreshState.Idle) hasPresented = true
    }
    return state
}

fun pagingScreenState(states: CombinedLoadStates, itemCount: Int, hasPresented: Boolean, errorMapper: (Throwable) -> UiError): ScreenState<Unit> {
    val error = (states.source.refresh as? LoadState.Error)?.error
        ?: (states.mediator?.refresh as? LoadState.Error)?.error
        ?: (states.refresh as? LoadState.Error)?.error
    val loading = states.source.refresh is LoadState.Loading || states.mediator?.refresh is LoadState.Loading || states.refresh is LoadState.Loading
    val available = itemCount > 0 || hasPresented
    return when {
        error != null && available -> ScreenState.Success(Unit, RefreshState.Error(errorMapper(error)))
        error != null -> ScreenState.Error(errorMapper(error))
        loading && available -> ScreenState.Success(Unit, RefreshState.Loading)
        loading -> ScreenState.Loading
        else -> ScreenState.Success(Unit)
    }
}
