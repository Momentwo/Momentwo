package cord.eoeo.momentwo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

interface UiState
interface UiEvent
interface UiEffect

const val SIDE_EFFECTS_KEY = "side_effects"

abstract class BaseViewModel<State : UiState, Event : UiEvent, Effect : UiEffect> : ViewModel() {
    private val initialState: State by lazy { createInitialState() }

    abstract fun createInitialState(): State

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    private val _effect: Channel<Effect> = Channel()
    val effect = _effect.receiveAsFlow()

    init {
        subscribeEvents()
    }

    protected fun setState(newState: State) {
        _uiState.value = newState
    }

    fun setEvent(newEvent: Event) {
        viewModelScope.launch { _event.emit(newEvent) }
    }

    private fun subscribeEvents() {
        viewModelScope.launch { event.collect { handleEvent(it) } }
    }

    abstract fun handleEvent(newEvent: Event)

    protected fun setEffect(newEffectBuilder: () -> Effect) {
        viewModelScope.launch { _effect.send(newEffectBuilder()) }
    }
}
