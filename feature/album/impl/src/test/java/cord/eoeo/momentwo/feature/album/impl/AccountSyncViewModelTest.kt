package cord.eoeo.momentwo.feature.album.impl
import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.testing.*
import cord.eoeo.momentwo.feature.album.impl.sync.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AccountSyncViewModelTest {
    @get:Rule val main=MainDispatcherRule(StandardTestDispatcher())
    private val store=ViewModelStore();private val accounts=AccountAccessFake();private val local=LocalLibraryFake()
    private val a=AccountIdentity("A","one",true);private val b=AccountIdentity("B","two",true)
    private val jobs=MutableStateFlow<List<SyncWork>>(emptyList())
    private val calls=mutableListOf<List<Any?>>()
    private var write:suspend ()->Unit={};private var readCandidates:suspend (String)->List<RemoteCandidate> = {listOf(RemoteCandidate(9,"remote"))}
    private var readInspection:suspend (String)->String={"remote"}
    private val sync=object:StrictAccountSyncOperations(){
        override fun observeWorks()=jobs
        override suspend fun requireOwner(expected:AccountIdentity):AccountIdentity {check(accounts.identity.value==expected);return expected}
        override suspend fun copyAlbums(ids:Set<String>,expected:AccountIdentity,intentId:String):String{calls+=listOf("copy",ids.toSet(),expected,intentId);write();return intentId}
        override suspend fun createAccountAlbum(title:String,intentId:String,expected:AccountIdentity?,inviteNames:List<String>):String{calls+=listOf("create",title,expected,intentId);write();return intentId}
        override suspend fun candidates(jobId:String,expected:AccountIdentity)=readCandidates(jobId)
        override suspend fun inspect(jobId:String,expected:AccountIdentity)=readInspection(jobId)
        override suspend fun bind(jobId:String,candidateId:Int,expected:AccountIdentity){calls+=listOf("bind",jobId,candidateId,expected);write()}
        override suspend fun keepMine(jobId:String,expected:AccountIdentity){calls+=listOf("mine",jobId,expected);write()}
        override suspend fun useServer(jobId:String,expected:AccountIdentity,observed:String){calls+=listOf("server",jobId,expected,observed);write()}
        override suspend fun retryConfirmed(jobId:String,expected:AccountIdentity){calls+=listOf("retry",jobId,expected);write()}
        override suspend fun cancel(jobId:String,expected:AccountIdentity){calls+=listOf("cancel",jobId,expected);write()}
        override suspend fun schedulePending(){calls+=listOf("schedule")}
    }
    private fun job(id:String="job",owner:String="A")=SyncWork(id,owner,null,"target",SyncKind.CREATE_ALBUM,SyncStatus.UNKNOWN,"album","mine",null)
    private fun vm()=AccountSyncViewModel(accounts,local,sync).also{store.put("sync",it);it.initialize()}
    @After fun clear() { store.clear(); main.testDispatcher.scheduler.runCurrent() }
    @Test fun onlyCurrentAccountJobsAreShownAndSwitchOrLogoutClearsPrivateInteraction()=runTest(main.testDispatcher){
        accounts.identity.value=a;jobs.value=listOf(job(),job("other","B"));val vm=vm();runCurrent();vm.initialize();runCurrent()
        assertEquals(listOf("job"),(vm.uiState.value.screen as ScreenState.Success).content.jobs.map{it.id})
        vm.onEvent(AccountSyncEvent.Toggle("local"));vm.onEvent(AccountSyncEvent.OpenCreate);vm.onEvent(AccountSyncEvent.Text("private A"))
        accounts.identity.value=b;runCurrent()
        assertEquals(listOf("other"),(vm.uiState.value.screen as ScreenState.Success).content.jobs.map{it.id})
        assertTrue(vm.uiState.value.selected.isEmpty());assertEquals(SyncDialog.None,vm.uiState.value.dialog)
        assertEquals("",vm.uiState.value.draft)
        accounts.identity.value=null;runCurrent();assertEquals(ScreenState.Error(UiError.Unauthorized),vm.uiState.value.screen)
    }
    @Test fun copySnapshotsIdsOwnerAndIntentAndDoesNotClearNewSelection()=runTest(main.testDispatcher){
        accounts.identity.value=a;val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(AccountSyncEvent.Toggle("first"));vm.onEvent(AccountSyncEvent.OpenCopy);val token=vm.uiState.value.token
        val gate=ControlledCall<Unit>();write={gate.await()};vm.onEvent(AccountSyncEvent.ConfirmCopy);runCurrent()
        vm.onEvent(AccountSyncEvent.Toggle("second"));vm.onEvent(AccountSyncEvent.ConfirmCopy);runCurrent();gate.complete(Unit);runCurrent()
        assertEquals(listOf(listOf("copy",setOf("first"),a,token)),calls)
        assertEquals(setOf("first","second"),vm.uiState.value.selected);assertEquals(SyncDialog.Copy,vm.uiState.value.dialog)
        assertEquals(1,effects.values.size);assertTrue(vm.uiState.value.pending.isEmpty())
    }
    @Test fun writeFailureKeepsDialogAndCancellationNeverPublishesFailure()=runTest(main.testDispatcher){
        accounts.identity.value=a;val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(AccountSyncEvent.OpenCreate);vm.onEvent(AccountSyncEvent.Text("draft"))
        write={throw IOException("disk")};vm.onEvent(AccountSyncEvent.Create);runCurrent()
        assertEquals("draft",vm.uiState.value.draft);assertEquals(SyncDialog.Create,vm.uiState.value.dialog);assertEquals(1,effects.values.size)
        write={throw CancellationException()};vm.onEvent(AccountSyncEvent.Create);runCurrent()
        assertEquals(1,effects.values.size);assertTrue(vm.uiState.value.pending.isEmpty())
    }
    @Test fun oldAccountCompletionAndViewModelClearCannotEmitNavigationOrMessage()=runTest(main.testDispatcher){
        accounts.identity.value=a;val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        val gate=ControlledCall<Unit>();write={gate.await()}
        vm.onEvent(AccountSyncEvent.OpenCreate);vm.onEvent(AccountSyncEvent.Text("A"));vm.onEvent(AccountSyncEvent.Create);runCurrent()
        accounts.identity.value=b;runCurrent();gate.complete(Unit);runCurrent();assertTrue(effects.values.isEmpty())
        vm.onEvent(AccountSyncEvent.OpenCreate);vm.onEvent(AccountSyncEvent.Text("B"));write={awaitCancellation()}
        vm.onEvent(AccountSyncEvent.Create);runCurrent();val before=vm.uiState.value;store.clear();runCurrent()
        assertEquals(before,vm.uiState.value);assertTrue(effects.values.isEmpty())
    }
    @Test fun oldCandidatesCannotOverwriteNewDialogAndFailuresStayInDialog()=runTest(main.testDispatcher){
        accounts.identity.value=a;val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        val old=ControlledCall<List<RemoteCandidate>>()
        readCandidates={if(it=="old")withContext(NonCancellable){old.await()}else listOf(RemoteCandidate(9,"new"))}
        vm.onEvent(AccountSyncEvent.Link(job("old")));runCurrent();vm.onEvent(AccountSyncEvent.Link(job("new")));runCurrent()
        old.complete(listOf(RemoteCandidate(1,"old")));runCurrent()
        val dialog=vm.uiState.value.dialog as SyncDialog.Link
        assertEquals("new",dialog.job.id);assertEquals(9,(dialog.candidates as ScreenState.Success).content.single().id)
        readCandidates={throw IOException("read")};vm.onEvent(AccountSyncEvent.Link(job("failed")));runCurrent()
        assertTrue((vm.uiState.value.dialog as SyncDialog.Link).candidates is ScreenState.Error)
        assertTrue(effects.values.isEmpty())
        vm.onEvent(AccountSyncEvent.Dismiss);runCurrent();assertEquals(SyncDialog.None,vm.uiState.value.dialog)
    }
    @Test fun explicitResolutionAndRetryAndCancelPassOnlyConfirmedJobAndOwner()=runTest(main.testDispatcher){
        accounts.identity.value=a;val vm=vm();runCurrent()
        vm.onEvent(AccountSyncEvent.Link(job()));runCurrent();vm.onEvent(AccountSyncEvent.Bind(9));runCurrent()
        vm.onEvent(AccountSyncEvent.Inspect(job()));runCurrent();vm.onEvent(AccountSyncEvent.Mine);runCurrent()
        vm.onEvent(AccountSyncEvent.Inspect(job()));runCurrent();vm.onEvent(AccountSyncEvent.Server);runCurrent()
        vm.onEvent(AccountSyncEvent.Link(job()));runCurrent();vm.onEvent(AccountSyncEvent.OpenRetry);vm.onEvent(AccountSyncEvent.RetryConfirmed);runCurrent()
        vm.onEvent(AccountSyncEvent.OpenCancel(job()));vm.onEvent(AccountSyncEvent.Cancel);runCurrent()
        assertEquals(listOf(listOf("bind","job",9,a),listOf("mine","job",a),listOf("server","job",a,"remote"),listOf("retry","job",a),listOf("cancel","job",a)),calls)
        assertTrue(vm.uiState.value.pending.isEmpty());assertEquals(SyncDialog.None,vm.uiState.value.dialog)
    }
    @Test fun successfulCreateNavigatesOnceAndBackgroundSchedulingNeedsExplicitRetry()=runTest(main.testDispatcher){
        accounts.identity.value=a;val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        assertTrue(calls.isEmpty());vm.onEvent(AccountSyncEvent.OpenCreate);vm.onEvent(AccountSyncEvent.Text("created"))
        val token=vm.uiState.value.token;vm.onEvent(AccountSyncEvent.Create);runCurrent()
        assertEquals(listOf(listOf("create","created",a,token)),calls)
        assertEquals(listOf(AccountSyncEffect.Album(token)),effects.values)
        vm.onEvent(AccountSyncEvent.Retry);runCurrent();assertEquals(listOf("schedule"),calls.last())
    }
}
