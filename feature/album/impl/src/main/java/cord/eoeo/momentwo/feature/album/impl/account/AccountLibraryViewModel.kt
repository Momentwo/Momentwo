package cord.eoeo.momentwo.feature.album.impl.account
import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
@HiltViewModel
internal class AccountLibraryViewModel @Inject constructor(private val accounts:AccountLibraryRepository,private val local:LocalLibraryRepository):ReducerViewModel<AccountLibraryState,AccountLibraryEvent,AccountLibraryAction,AccountLibraryEffect>(AccountLibraryState(),AccountLibraryReducer){
    private data object Refresh:RequestKey;private data object Logout:RequestKey
    private var observation:Job?=null
    @OptIn(ExperimentalCoroutinesApi::class)
    fun initialize(){if(observation?.isActive==true)return;observation=viewModelScope.launch{
        runSuspendCatching { local.initialize() }.onFailure { Log.e("AccountLibrary","Storage initialization failed",it);dispatch(AccountLibraryAction.Failure(UiError.Unknown));return@launch }
        accounts.identities().flatMapLatest { identity ->
            requests.cancel(Refresh)
            val storage=accounts.storage()
            combine(local.albums(LibraryScope.Device),if(identity==null)flowOf(emptyList())else local.albums(LibraryScope.Account(identity.accountId))){device,albums->AccountLibraryContent(identity,albums,device.size,storage.first,storage.second)}
        }.catch{if(it is CancellationException||it is Error)throw it;Log.e("AccountLibrary","Read failed",it);dispatch(AccountLibraryAction.Failure(UiError.Unknown))}.collect{dispatch(AccountLibraryAction.Snapshot(it))}
    }}
    fun openAlbums() {
        initialize()
        viewModelScope.launch { if(accounts.identities().first()?.canSync==true)refresh() }
    }
    override fun handleEvent(event:AccountLibraryEvent){when(event){
        AccountLibraryEvent.Retry->{initialize();if((uiState.value.screen as? ScreenState.Success)?.content?.identity?.canSync==true) refresh()}
        AccountLibraryEvent.Login->sendEffect(AccountLibraryEffect.Login)
        AccountLibraryEvent.Back->sendEffect(AccountLibraryEffect.Back)
        AccountLibraryEvent.Sync->sendEffect(AccountLibraryEffect.Sync)
        AccountLibraryEvent.Friend->if((uiState.value.screen as? ScreenState.Success)?.content?.identity!=null)sendEffect(AccountLibraryEffect.Friend)else sendEffect(AccountLibraryEffect.Login)
        is AccountLibraryEvent.Open->sendEffect(AccountLibraryEffect.Open(event.id))
        AccountLibraryEvent.Logout->requests.launchRequest(Logout,RequestPolicy.IgnoreWhileRunning,{runSuspendCatching{accounts.logout()}},onStarted={dispatch(AccountLibraryAction.Pending("logout",true))},onSuccess={},onFailure={Log.e("AccountLibrary","Logout failed",it);emitEffect(AccountLibraryEffect.Message("계정 연결을 해제하지 못했습니다"))},onFinished={dispatch(AccountLibraryAction.Pending("logout",false))})
    }}
    private fun refresh(){requests.launchRequest(Refresh,RequestPolicy.LatestWins,{runSuspendCatching{accounts.refresh()}},onStarted={dispatch(AccountLibraryAction.Refreshing)},onSuccess={dispatch(AccountLibraryAction.Refreshed)},onFailure={Log.e("AccountLibrary","Account read failed",it);dispatch(AccountLibraryAction.Failure(it.toUiError()))})}
}
