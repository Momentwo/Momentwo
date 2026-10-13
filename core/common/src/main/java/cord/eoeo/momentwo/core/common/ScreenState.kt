package cord.eoeo.momentwo.core.common

sealed interface ScreenState<out T : Any> {
    data object Loading : ScreenState<Nothing>
    data class Success<T : Any>(val content: T, val refresh: RefreshState = RefreshState.Idle) : ScreenState<T>
    data class Error(val error: UiError) : ScreenState<Nothing>
}

sealed interface RefreshState {
    data object Idle : RefreshState
    data object Loading : RefreshState
    data class Error(val error: UiError) : RefreshState
}

sealed interface UiError {
    val canRetry: Boolean
    data object Network : UiError { override val canRetry = true }
    data object Server : UiError { override val canRetry = true }
    data object Unauthorized : UiError { override val canRetry = false }
    data object Forbidden : UiError { override val canRetry = false }
    data object NotFound : UiError { override val canRetry = false }
    data object Unknown : UiError { override val canRetry = true }
}

fun <T : Any> ScreenState<T>.startRefresh(): ScreenState<T> = when (this) {
    ScreenState.Loading -> ScreenState.Loading
    is ScreenState.Error -> ScreenState.Loading
    is ScreenState.Success -> copy(refresh = RefreshState.Loading)
}

fun <T : Any> ScreenState<T>.failRefresh(error: UiError): ScreenState<T> = when (this) {
    ScreenState.Loading -> ScreenState.Error(error)
    is ScreenState.Error -> ScreenState.Error(error)
    is ScreenState.Success -> copy(refresh = RefreshState.Error(error))
}

fun <T : Any> ScreenState<T>.cancelRefresh(): ScreenState<T> = when (this) {
    ScreenState.Loading -> this
    is ScreenState.Error -> this
    is ScreenState.Success -> if (refresh == RefreshState.Loading) copy(refresh = RefreshState.Idle) else this
}
