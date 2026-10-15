package cord.eoeo.momentwo.feature.auth.impl
import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.testing.*
import cord.eoeo.momentwo.feature.auth.impl.profile.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule val main=MainDispatcherRule(StandardTestDispatcher())
    private val store=ViewModelStore()
    @After fun clear(){store.clear();main.testDispatcher.scheduler.runCurrent()}
    @Test fun nullableAndNamedProfilesRetainTheirInitialIdentity()=runTest(main.testDispatcher){
        for(nickname in listOf(null,"fixture")){
            val vm=ProfileViewModel(ProfileState(nickname)).also{store.put("profile",it)}
            assertEquals(nickname,vm.uiState.value.nickname);runCurrent();assertEquals(ProfileState(nickname),vm.uiState.value)
        }
    }
    @Test fun errorEffectsRemainOrderedWithoutChangingProfileState()=runTest(main.testDispatcher){
        val vm=ProfileViewModel(ProfileState("fixture")).also{store.put("profile",it)}
        val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(ProfileEvent.OnError("first"));vm.onEvent(ProfileEvent.OnError("second"));runCurrent()
        assertEquals(listOf(ProfileEffect.ShowSnackbar("first"),ProfileEffect.ShowSnackbar("second")),effects.values)
        assertEquals(ProfileState("fixture"),vm.uiState.value);runCurrent();assertEquals(2,effects.values.size)
    }
    @Test fun clearedProfileIgnoresNewEvents()=runTest(main.testDispatcher){
        val vm=ProfileViewModel(ProfileState()).also{store.put("profile",it)}
        val effects=EffectRecorder(vm.effect,backgroundScope);store.clear()
        vm.onEvent(ProfileEvent.OnError("late"));runCurrent()
        assertTrue(effects.values.isEmpty());assertEquals(ProfileState(),vm.uiState.value)
    }
}
