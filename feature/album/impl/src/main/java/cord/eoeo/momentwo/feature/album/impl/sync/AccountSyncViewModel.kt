package cord.eoeo.momentwo.feature.album.impl.sync
import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
@HiltViewModel
internal class AccountSyncViewModel @Inject constructor(private val account:AccountLibraryAccess,private val local:LocalLibraryRepository,private val sync:AccountSyncOperations):ReducerViewModel<AccountSyncState,AccountSyncEvent,AccountSyncAction,AccountSyncEffect>(AccountSyncState(),AccountSyncReducer){
    private data class Write(val key:String):RequestKey;private data object Read:RequestKey
    private var observation:Job?=null
    @OptIn(ExperimentalCoroutinesApi::class)
    fun initialize(){if(observation?.isActive==true)return;observation=viewModelScope.launch{
        account.identities().flatMapLatest{identity->requests.cancel(Read);if(identity==null)flowOf<AccountSyncContent?>(null)else combine(local.albums(LibraryScope.Device),sync.observeWorks()){albums,jobs->AccountSyncContent(identity,albums,jobs.filter{it.accountId==identity.accountId})}}
        .catch{if(it is CancellationException || it is Error)throw it;Log.e("AccountSyncUI","Stored jobs could not load",it);dispatch(AccountSyncAction.Error(UiError.Unknown))}
        .collect{if(it==null)dispatch(AccountSyncAction.Error(UiError.Unauthorized))else dispatch(AccountSyncAction.Snapshot(it))}
    }}
    private fun identity()=(uiState.value.screen as? ScreenState.Success)?.content?.identity
    override fun handleEvent(event:AccountSyncEvent){when(event){
        AccountSyncEvent.Back->sendEffect(AccountSyncEffect.Back)
        AccountSyncEvent.Retry->{initialize();viewModelScope.launch{runSuspendCatching{sync.schedulePending()}.onFailure{Log.e("AccountSyncUI","Schedule failed",it)}}}
        AccountSyncEvent.Dismiss->{requests.cancel(Read);dispatch(AccountSyncAction.Dismiss)}
        is AccountSyncEvent.Toggle->dispatch(AccountSyncAction.Select(event.id,UUID.randomUUID().toString()))
        AccountSyncEvent.OpenCopy->if(uiState.value.selected.isNotEmpty())dispatch(AccountSyncAction.Open(SyncDialog.Copy,UUID.randomUUID().toString()))
        AccountSyncEvent.ConfirmCopy->{val owner=identity() ?: return;val state=uiState.value;if(state.dialog!=SyncDialog.Copy)return;write("copy",state.token,{sync.copyAlbums(state.selected,owner,state.token)},{emitEffect(AccountSyncEffect.Message("기기 원본을 유지하며 복사 작업을 저장했습니다"))})}
        AccountSyncEvent.OpenCreate->dispatch(AccountSyncAction.Open(SyncDialog.Create,UUID.randomUUID().toString()))
        is AccountSyncEvent.Text->dispatch(AccountSyncAction.Edited(event.value,UUID.randomUUID().toString()))
        AccountSyncEvent.Create->{val owner=identity() ?: return;val state=uiState.value;if(state.draft.isBlank() || state.dialog!=SyncDialog.Create)return;write("create",state.token,{sync.createAccountAlbum(state.draft,state.token,owner)},{id->if(uiState.value.dialog==SyncDialog.None)emitEffect(AccountSyncEffect.Album(id))})}
        is AccountSyncEvent.Link->{val owner=identity() ?: return;dispatch(AccountSyncAction.Open(SyncDialog.Link(event.job),UUID.randomUUID().toString()));requests.launchRequest(Read,RequestPolicy.LatestWins,{runSuspendCatching{sync.candidates(event.job.id,owner)}},onSuccess={dispatch(AccountSyncAction.Candidates(event.job.id,it))},onFailure={Log.e("AccountSyncUI","Candidates failed",it);dispatch(AccountSyncAction.ReadFailed(event.job.id,it.toUiError()))})}
        is AccountSyncEvent.Inspect->{val owner=identity() ?: return;dispatch(AccountSyncAction.Open(SyncDialog.Inspect(event.job),UUID.randomUUID().toString()));requests.launchRequest(Read,RequestPolicy.LatestWins,{runSuspendCatching{sync.inspect(event.job.id,owner)}},onSuccess={dispatch(AccountSyncAction.Inspected(event.job.id,it))},onFailure={Log.e("AccountSyncUI","Inspection failed",it);dispatch(AccountSyncAction.ReadFailed(event.job.id,it.toUiError()))})}
        is AccountSyncEvent.Bind->{val owner=identity() ?: return;val dialog=uiState.value.dialog as? SyncDialog.Link ?: return;if(dialog.candidates !is ScreenState.Success)return;write("link",uiState.value.token,{sync.bind(dialog.job.id,event.id,owner)},{emitEffect(AccountSyncEffect.Message("선택한 서버 항목을 연결했습니다"))})}
        AccountSyncEvent.Mine->{val owner=identity() ?: return;val dialog=uiState.value.dialog as? SyncDialog.Inspect ?: return;if(dialog.value !is ScreenState.Success)return;write("resolve",uiState.value.token,{sync.keepMine(dialog.job.id,owner)},{})}
        AccountSyncEvent.Server->{val owner=identity() ?: return;val dialog=uiState.value.dialog as? SyncDialog.Inspect ?: return;val value=(dialog.value as? ScreenState.Success)?.content ?: return;write("resolve",uiState.value.token,{sync.useServer(dialog.job.id,owner,value)},{})}
        AccountSyncEvent.OpenRetry->{val job=when(val dialog=uiState.value.dialog){is SyncDialog.Link->if(dialog.candidates is ScreenState.Success)dialog.job else return;is SyncDialog.Inspect->if(dialog.value is ScreenState.Success)dialog.job else return;SyncDialog.None,SyncDialog.Copy,SyncDialog.Create,is SyncDialog.Retry,is SyncDialog.Cancel->return};dispatch(AccountSyncAction.Open(SyncDialog.Retry(job),UUID.randomUUID().toString()))}
        AccountSyncEvent.RetryConfirmed->{val owner=identity() ?: return;val dialog=uiState.value.dialog as? SyncDialog.Retry ?: return;write("retry",uiState.value.token,{sync.retryConfirmed(dialog.job.id,owner)},{})}
        is AccountSyncEvent.OpenCancel->dispatch(AccountSyncAction.Open(SyncDialog.Cancel(event.job),UUID.randomUUID().toString()))
        AccountSyncEvent.Cancel->{val owner=identity() ?: return;val dialog=uiState.value.dialog as? SyncDialog.Cancel ?: return;write("cancel",uiState.value.token,{sync.cancel(dialog.job.id,owner)},{})}
    }}
    private fun <T> write(key:String,token:String,request:suspend()->T,success:suspend(T)->Unit){val owner=identity() ?: return;requests.launchRequest(Write(key),RequestPolicy.IgnoreWhileRunning,{runSuspendCatching{sync.requireOwner(owner);request()}},onStarted={dispatch(AccountSyncAction.Pending(key,true))},onSuccess={if(identity()==owner){dispatch(AccountSyncAction.Finished(token));success(it)}},onFailure={Log.e("AccountSyncUI","Local task failed",it);if(identity()==owner)emitEffect(AccountSyncEffect.Message(it.message ?: "작업을 저장하지 못했습니다"))},onFinished={dispatch(AccountSyncAction.Pending(key,false))})}
}
