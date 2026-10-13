package cord.eoeo.momentwo.core.common

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

abstract class ReducerViewModel<State : UiState, Event : UiEvent, Action, Effect : UiEffect>(
    initialState: State,
    private val reducer: Reducer<State, Action>,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    val uiState = mutableState.asStateFlow()
    private val effects = Channel<Effect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()
    protected val requests = RequestController(viewModelScope)

    @MainThread
    fun onEvent(event: Event) { if (viewModelScope.isActive) handleEvent(event) }

    protected abstract fun handleEvent(event: Event)

    @MainThread
    protected fun dispatch(action: Action) {
        if (viewModelScope.isActive) mutableState.update { reducer.reduce(it, action) }
    }

    protected suspend fun emitEffect(effect: Effect) {
        currentCoroutineContext().ensureActive()
        if (viewModelScope.isActive) effects.send(effect)
    }

    protected fun sendEffect(effect: Effect) { viewModelScope.launch { emitEffect(effect) } }

    override fun onCleared() {
        requests.close()
        effects.cancel()
        super.onCleared()
    }
}
