package cord.eoeo.momentwo.feature.album.impl.local

import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

@HiltViewModel
internal class LocalLibraryViewModel @Inject constructor(private val repository: LocalLibraryRepository, private val drafts: LibraryDrafts) :
    ReducerViewModel<LocalLibraryState, LocalLibraryEvent, LocalLibraryAction, LocalLibraryEffect>(LocalLibraryState(), LocalLibraryReducer) {
    private data object Create : RequestKey
    private data object DraftWrite : RequestKey
    private val draftKey = "device:create-album"
    private var observation: Job? = null
    fun initialize() {
        if (observation?.isActive == true) return
        observation = viewModelScope.launch {
            runSuspendCatching {
                repository.initialize(); repository.cleanUnusedMedia()
            }.onFailure { Log.e("LocalLibrary", "Initialization failed", it); dispatch(LocalLibraryAction.Failed); return@launch }
            runSuspendCatching { drafts.load(draftKey) }.onSuccess { it?.let { draft -> dispatch(LocalLibraryAction.Restored(draft)) } }
                .onFailure { Log.e("LocalLibrary", "Draft restore failed", it); emitEffect(LocalLibraryEffect.Message("초안을 불러오지 못했습니다")) }
            repository.albums(LibraryScope.Device).catch {
                if (it is kotlinx.coroutines.CancellationException || it is Error) throw it
                Log.e("LocalLibrary", "Local observation failed", it); dispatch(LocalLibraryAction.Failed)
            }.collect { dispatch(LocalLibraryAction.Loaded(it)) }
        }
    }
    override fun handleEvent(event: LocalLibraryEvent) {
        when (event) {
            LocalLibraryEvent.Retry -> initialize()
            LocalLibraryEvent.ConnectAccount -> sendEffect(LocalLibraryEffect.Login)
            LocalLibraryEvent.OpenCreate -> dispatch(LocalLibraryAction.Open(LibraryDraft("", "", UUID.randomUUID().toString())))
            LocalLibraryEvent.Dismiss -> {
                val token = uiState.value.draft.token; requests.cancel(DraftWrite); dispatch(LocalLibraryAction.Dismiss)
                viewModelScope.launch { runSuspendCatching { drafts.clearIfMatches(draftKey, token) } }
            }
            is LocalLibraryEvent.TextChanged -> {
                val draft = LibraryDraft(event.text, "", UUID.randomUUID().toString()); dispatch(LocalLibraryAction.Edited(draft))
                requests.launchRequest(DraftWrite, RequestPolicy.LatestWins, { runSuspendCatching { drafts.save(draftKey, draft) } }, onSuccess = {}, onFailure = { Log.e("LocalLibrary", "Draft save failed", it); emitEffect(LocalLibraryEffect.Message("초안을 저장하지 못했습니다")) })
            }
            LocalLibraryEvent.Save -> {
                val draft = uiState.value.draft
                val interaction = uiState.value.interaction
                if (draft.text.isBlank()) return
                requests.launchRequest(Create, RequestPolicy.IgnoreWhileRunning, request = { runSuspendCatching {
                    drafts.save(draftKey, draft); repository.createAlbum(draft.text, draft.token)
                } }, onStarted = { requests.cancel(DraftWrite); dispatch(LocalLibraryAction.Pending(true)) },
                    onSuccess = { id ->
                        dispatch(LocalLibraryAction.Created(draft.token))
                        runSuspendCatching { drafts.clearIfMatches(draftKey, draft.token) }.onFailure { Log.e("LocalLibrary", "Committed; draft cleanup failed", it) }
                        if (!uiState.value.creating && uiState.value.draft.token == draft.token && uiState.value.interaction == interaction) emitEffect(LocalLibraryEffect.Album(id))
                        else emitEffect(LocalLibraryEffect.Message("앨범을 기기에 저장했습니다"))
                    }, onFailure = { Log.e("LocalLibrary", "Create failed", it); emitEffect(LocalLibraryEffect.Message("앨범을 저장하지 못했습니다")) }, onFinished = { dispatch(LocalLibraryAction.Pending(false)) })
            }
            is LocalLibraryEvent.OpenAlbum -> sendEffect(LocalLibraryEffect.Album(event.id))
        }
    }
}
