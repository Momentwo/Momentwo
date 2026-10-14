package cord.eoeo.momentwo.feature.photo.impl.local
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.LocalLibraryRepository
import cord.eoeo.momentwo.core.data.library.AccountLibraryRepository
import cord.eoeo.momentwo.core.data.library.LibraryDrafts
import cord.eoeo.momentwo.core.model.LibraryDraft
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.feature.photo.api.LocalPhotoListDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID
@HiltViewModel
internal class LocalPhotoListViewModel(private val repository: LocalLibraryRepository, id: String, private val accounts: AccountLibraryRepository? = null, private val drafts: LibraryDrafts? = null) : ReducerViewModel<LocalPhotoListState, LocalPhotoListEvent, LocalPhotoListAction, LocalPhotoListEffect>(LocalPhotoListState(id), LocalPhotoListReducer) {
    @Inject constructor(repository: LocalLibraryRepository, accounts: AccountLibraryRepository, drafts: LibraryDrafts, handle: SavedStateHandle) : this(repository, handle.toRoute<LocalPhotoListDestination>().subAlbumId,accounts,drafts)
    private data class Write(val key: String): RequestKey
    private data object DraftWrite: RequestKey
    private val draftKey="sub:${id}:rename"
    private data object Import : RequestKey
    private data object ReadAccount : RequestKey
    private var observation: Job? = null
    @OptIn(ExperimentalCoroutinesApi::class)
    fun initialize() {
        if(observation?.isActive == true) return
        refreshAccount()
        observation = viewModelScope.launch {
            val restored=runSuspendCatching { drafts?.load(draftKey) }.getOrNull()
            if(restored!=null && uiState.value.revision==0L)dispatch(LocalPhotoListAction.Draft(restored))
            combine(repository.subAlbum(uiState.value.id), repository.photos(uiState.value.id)) { sub, photos -> sub to photos }
            .flatMapLatest { (sub,photos) -> if(sub==null)flowOf(null)else repository.album(sub.albumId).map { album -> if(album==null)null else LocalPhotoListContent(sub,photos,album) } }
            .catch { if(it is CancellationException || it is Error) throw it; Log.e("LocalPhotos", "Observation failed", it); dispatch(LocalPhotoListAction.Failed(UiError.Unknown)) }
            .collect { if(it == null) dispatch(LocalPhotoListAction.Failed(UiError.NotFound)) else if(it.subAlbum.photosLoaded || it.photos.isNotEmpty()) dispatch(LocalPhotoListAction.Snapshot(it)) }
        }
    }
    private fun refreshAccount(){val remote=accounts ?: return;requests.launchRequest(ReadAccount,RequestPolicy.LatestWins,{runSuspendCatching{remote.refreshPhotos(uiState.value.id)}},onStarted={dispatch(LocalPhotoListAction.Refreshing)},onSuccess={dispatch(LocalPhotoListAction.Refreshed)},onFailure={Log.e("LocalPhotos","Account refresh failed",it);dispatch(LocalPhotoListAction.Failed(it.toUiError()))})}
    override fun handleEvent(event: LocalPhotoListEvent) {
        when(event) {
            is LocalPhotoListEvent.Menu -> dispatch(LocalPhotoListAction.Menu(event.open))
            is LocalPhotoListEvent.Edit -> dispatch(LocalPhotoListAction.Edit(event.enabled))
            is LocalPhotoListEvent.Select -> dispatch(LocalPhotoListAction.Select(event.id,event.checked))
            LocalPhotoListEvent.Rename -> {val content=(uiState.value.screen as? ScreenState.Success)?.content ?: return;dispatch(LocalPhotoListAction.Draft(LibraryDraft(content.subAlbum.title,"",UUID.randomUUID().toString())))}
            is LocalPhotoListEvent.Text -> {
                val draft=uiState.value.draft.copy(text=event.text,token=UUID.randomUUID().toString());dispatch(LocalPhotoListAction.Draft(draft))
                requests.launchRequest(DraftWrite,RequestPolicy.LatestWins,{runSuspendCatching { drafts?.save(draftKey,draft) }},onSuccess={},onFailure={emitEffect(LocalPhotoListEffect.Message("초안을 저장하지 못했습니다"))})
            }
            LocalPhotoListEvent.Dismiss -> {val token=uiState.value.draft.token;requests.cancel(DraftWrite);dispatch(LocalPhotoListAction.Dismiss);viewModelScope.launch{runSuspendCatching{drafts?.clearIfMatches(draftKey,token)}}}
            LocalPhotoListEvent.SaveName -> {
                val state=uiState.value;if(state.draft.text.isBlank())return
                write("rename",{drafts?.save(draftKey,state.draft);repository.editSubAlbum(state.id,state.draft.text)},
                    {dispatch(LocalPhotoListAction.Saved(state.draft.token));runSuspendCatching{drafts?.clearIfMatches(draftKey,state.draft.token)}})
            }
            LocalPhotoListEvent.Delete -> if(uiState.value.selected.isNotEmpty())dispatch(LocalPhotoListAction.ConfirmDelete)
            LocalPhotoListEvent.ConfirmDelete -> {
                val state=uiState.value;if(!state.confirmDelete || state.selected.isEmpty())return
                write("delete",{repository.deletePhotos(state.id,state.selected)},{dispatch(LocalPhotoListAction.Deleted(state.selected,state.revision))})
            }

            LocalPhotoListEvent.Retry -> {initialize();refreshAccount()}
            LocalPhotoListEvent.Back -> if(uiState.value.editing)dispatch(LocalPhotoListAction.Edit(false))else sendEffect(LocalPhotoListEffect.Back)
            is LocalPhotoListEvent.Open -> sendEffect(LocalPhotoListEffect.Open(event.id))
            is LocalPhotoListEvent.Import -> {
                if(event.sources.isEmpty() || uiState.value.screen !is ScreenState.Success) return
                val library=(uiState.value.screen as ScreenState.Success).content.album?.libraryId ?: return
                val id = uiState.value.id
                val sources = event.sources.map { it to UUID.randomUUID().toString() }
                requests.launchRequest(Import, RequestPolicy.IgnoreWhileRunning, { runSuspendCatching {
                    val owner=if(library=="device")null else requireNotNull(accounts).identities().first()
                    check(library=="device" || library==owner?.accountId?.let { "account:$it" })
                    var saved = 0
                    sources.forEach { (uri, intentId) -> runSuspendCatching { if(owner!=null)check(accounts?.identities()?.first()==owner);repository.importPhoto(id,uri,intentId) }.onSuccess { saved++ }.onFailure { Log.e("LocalPhotos", "Import failed", it) } }
                    saved to owner
                } }, onStarted = { dispatch(LocalPhotoListAction.Importing(true)) }, onSuccess = { (saved,owner) -> if(owner!=null && accounts?.identities()?.first()!=owner)return@launchRequest;emitEffect(LocalPhotoListEffect.Message("${saved}장의 사진을 기기에 저장했습니다" + if(saved < sources.size) "; 나머지 사진은 가져오지 못했습니다" else "")) }, onFailure = { Log.e("LocalPhotos", "Import failed", it); emitEffect(LocalPhotoListEffect.Message("사진을 보관하지 못했습니다")) }, onFinished = { dispatch(LocalPhotoListAction.Importing(false)) })
            }
        }
    }
    private fun write(key: String, block: suspend () -> Unit, success: suspend () -> Unit) {
        val library=(uiState.value.screen as? ScreenState.Success)?.content?.album?.libraryId
        suspend fun owned()=library=="device" || library==accounts?.identities()?.first()?.accountId?.let { "account:$it" }
        requests.launchRequest(Write(key),RequestPolicy.IgnoreWhileRunning,{runSuspendCatching{block()}},onStarted={if(key=="rename")requests.cancel(DraftWrite);dispatch(LocalPhotoListAction.Pending(key,true))},
            onSuccess={if(owned())success()},onFailure={Log.e("LocalPhotos","Write failed",it);if(owned())emitEffect(LocalPhotoListEffect.Message("변경을 기기에 저장하지 못했습니다"))},onFinished={dispatch(LocalPhotoListAction.Pending(key,false))})
    }
}
