package cord.eoeo.momentwo.feature.album.impl
import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.domain.friend.GetFriendListUseCase
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.testing.*
import cord.eoeo.momentwo.feature.album.impl.local.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryCreateAccountViewModelTest {
    @get:Rule val main=MainDispatcherRule(StandardTestDispatcher())
    private val store=ViewModelStore(); private val accounts=AccountAccessFake()
    private val local=LocalLibraryFake(); private val drafts=EmptyLibraryDrafts()
    private val a=AccountIdentity("A","one",true)
    private val requests=mutableListOf<List<Any?>>()
    private var create:suspend ()->Unit={}
    private val sync=object:StrictAccountSyncOperations(){
        override suspend fun createAccountAlbum(title:String,intentId:String,expected:AccountIdentity?,inviteNames:List<String>):String {
            requests+=listOf(title,intentId,expected,inviteNames.toList());create();return intentId
        }
    }
    private fun vm()=LibraryCreateViewModel(local,drafts,true,accounts,sync,GetFriendListUseCase(AlbumFriendRepository())).also{store.put("create",it);it.initialize()}
    @After fun clear() { store.clear(); main.testDispatcher.scheduler.runCurrent() }
    @Test fun submissionCapturesOwnerEpochAndInvitationsAndKeepsLaterDraft()=runTest(main.testDispatcher){
        accounts.identity.value=a;val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(LibraryCreateEvent.Text("first"));vm.onEvent(LibraryCreateEvent.Toggle("invited"));runCurrent()
        val token=vm.uiState.value.draft.token;val gate=ControlledCall<Unit>();create={gate.await()}
        vm.onEvent(LibraryCreateEvent.Submit);runCurrent();vm.onEvent(LibraryCreateEvent.Text("next"));vm.onEvent(LibraryCreateEvent.Submit);runCurrent()
        gate.complete(Unit);runCurrent()
        assertEquals(listOf(listOf("first",token,a,listOf("invited"))),requests)
        assertEquals("next",vm.uiState.value.draft.text);assertEquals("next",drafts.value?.text)
        assertTrue(effects.values.none{it is LibraryCreateEffect.Created});assertFalse(vm.uiState.value.pending)
        assertTrue(local.created.isEmpty())
    }
    @Test fun switchDuringSubmissionSuppressesOldAccountNavigationAndKeepsOriginalDraft()=runTest(main.testDispatcher){
        accounts.identity.value=a;val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(LibraryCreateEvent.Text("A private"));runCurrent();val gate=ControlledCall<Unit>();create={gate.await()}
        vm.onEvent(LibraryCreateEvent.Submit);runCurrent();accounts.identity.value=AccountIdentity("B","two",true);runCurrent()
        assertFalse(vm.uiState.value.ownerAvailable);gate.complete(Unit);runCurrent();assertTrue(effects.values.isEmpty())
        vm.onEvent(LibraryCreateEvent.Submit);runCurrent();assertEquals(1,requests.size)
        accounts.identity.value=a.copy(epoch="new");runCurrent();assertTrue(vm.uiState.value.ownerAvailable)
        assertEquals("A private",vm.uiState.value.draft.text)
    }
    @Test fun failedCreationRetainsDraftAndCancelledRetryDoesNotEmitFailure()=runTest(main.testDispatcher){
        accounts.identity.value=a;val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(LibraryCreateEvent.Text("retained"));runCurrent();create={throw IOException("storage")}
        vm.onEvent(LibraryCreateEvent.Submit);runCurrent();assertEquals("retained",vm.uiState.value.draft.text)
        assertEquals(1,effects.values.size);assertFalse(vm.uiState.value.pending)
        create={throw CancellationException()};vm.onEvent(LibraryCreateEvent.Submit);runCurrent()
        assertEquals(1,effects.values.size);assertFalse(vm.uiState.value.pending)
    }
    @Test fun oldAccountCreationFailureCannotShowMessageAfterSwitch()=runTest(main.testDispatcher){
        accounts.identity.value=a;val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(LibraryCreateEvent.Text("old A"));runCurrent();val gate=ControlledCall<Unit>();create={gate.await()}
        vm.onEvent(LibraryCreateEvent.Submit);runCurrent();accounts.identity.value=AccountIdentity("B","two",true);runCurrent()
        gate.fail(IOException("old A failed"));runCurrent()
        assertTrue(effects.values.isEmpty());assertFalse(vm.uiState.value.pending)
    }

}
