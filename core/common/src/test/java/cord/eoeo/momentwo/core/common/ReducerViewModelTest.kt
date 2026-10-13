package cord.eoeo.momentwo.core.common

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReducerViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private data class State(val name: String = "", val count: Int = 0) : UiState
    private sealed interface Event : UiEvent {
        data class Name(val name: String) : Event
        data object Increment : Event
        data class Message(val text: String) : Event
    }
    private data class Effect(val text: String) : UiEffect
    private class TestViewModel : ReducerViewModel<State, Event, Event, Effect>(State(), Reducer { state, event ->
        when (event) {
            is Event.Name -> state.copy(name = event.name)
            Event.Increment -> state.copy(count = state.count + 1)
            is Event.Message -> state
        }
    }) {
        override fun handleEvent(event: Event) {
            when (event) {
                is Event.Message -> sendEffect(Effect(event.text))
                is Event.Name, Event.Increment -> dispatch(event)
            }
        }
    }

    @Test fun updatesAreImmediateAndPreserveUnrelatedFields() = runTest(main.testDispatcher) {
        val vm = TestViewModel()
        val store = ViewModelStore().apply { put("test", vm) }
        vm.onEvent(Event.Name("edited"))
        repeat(10) { vm.onEvent(Event.Increment) }
        assertEquals(State("edited", 10), vm.uiState.value)
        store.clear()
        vm.onEvent(Event.Increment)
        assertEquals(State("edited", 10), vm.uiState.value)
    }

    @Test fun effectsRemainBufferedBeforeUiCollectorStarts() = runTest(main.testDispatcher) {
        val vm = TestViewModel()
        val store = ViewModelStore().apply { put("test", vm) }
        vm.onEvent(Event.Message("first"))
        vm.onEvent(Event.Message("second"))
        advanceUntilIdle()
        assertEquals(Effect("first"), vm.effect.first())
        assertEquals(Effect("second"), vm.effect.first())
        store.clear()
    }
}
