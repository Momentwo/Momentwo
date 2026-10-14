package cord.eoeo.momentwo.feature.albumdetail.impl.local

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.model.LibraryDraft
import cord.eoeo.momentwo.feature.albumdetail.api.LocalAlbumDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
internal class LocalAlbumViewModel(private val repository: LocalLibraryRepository, private val drafts: LibraryDrafts, id: String, private val accounts: AccountLibraryRepository? = null) :
    ReducerViewModel<LocalAlbumState, LocalAlbumEvent, LocalAlbumAction, LocalAlbumEffect>(LocalAlbumState(id), LocalAlbumReducer) {
    @Inject constructor(repository: LocalLibraryRepository, drafts: LibraryDrafts, accounts: AccountLibraryRepository, handle: SavedStateHandle) : this(repository, drafts, handle.toRoute<LocalAlbumDetailDestination>().localId,accounts)
    private data class Write(val key: String) : RequestKey
    private data object DraftWrite : RequestKey
    private data object ReadAccount : RequestKey
    private val draftKey = "device:album:$id"
    private var observation: Job? = null
    fun initialize() {
        if (observation?.isActive == true) return
        refreshAccount()
        observation = viewModelScope.launch {
            val saved = runSuspendCatching { drafts.load(draftKey) }.getOrElse { Log.e("LocalAlbum", "Draft restore failed", it); null }
            var restored = false
            combine(repository.album(uiState.value.id), repository.subAlbums(uiState.value.id)) { album, subs -> album to subs }
                .catch { if (it is CancellationException || it is Error) throw it; Log.e("LocalAlbum", "Observation failed", it); dispatch(LocalAlbumAction.Failed(UiError.Unknown)) }
                .collect { (album,subs) ->
                    val content = album?.let { LocalAlbumContent(it,subs) }
                    if (content == null) dispatch(LocalAlbumAction.Failed(UiError.NotFound)) else if(content.album.libraryId == "device" || (content.album.subsLoaded && content.album.role != null)) {
                        dispatch(LocalAlbumAction.Snapshot(content))
                        if (!restored && saved != null) {
                            val dialog = when {
                                saved.context == "create-sub" -> LocalAlbumDialog.CreateSub
                                saved.context == "edit-album" -> LocalAlbumDialog.EditAlbum
                                saved.context.startsWith("rename:") && content.subAlbums.any { it.localId == saved.context.removePrefix("rename:") } -> LocalAlbumDialog.RenameSub(saved.context.removePrefix("rename:"))
                                else -> LocalAlbumDialog.None
                            }
                            if (dialog != LocalAlbumDialog.None) dispatch(LocalAlbumAction.Restored(dialog, saved))
                        }
                        restored = true
                    }
                }
        }
    }
    private fun refreshAccount() {
        val remote = accounts ?: return
        requests.launchRequest(ReadAccount,RequestPolicy.LatestWins,{runSuspendCatching { val album=repository.album(uiState.value.id).first(); if(album != null && album.libraryId != "device") remote.refreshAlbum(album.localId) }},onSuccess={},onFailure={ Log.e("LocalAlbum","Account refresh failed",it);dispatch(LocalAlbumAction.Failed(it.toUiError())) })
    }
    override fun handleEvent(event: LocalAlbumEvent) {
        when (event) {
            LocalAlbumEvent.Retry -> { initialize(); refreshAccount() }
            is LocalAlbumEvent.Open -> {
                val content = (uiState.value.screen as? ScreenState.Success)?.content ?: return
                val draft = when (val dialog = event.dialog) {
                    LocalAlbumDialog.EditAlbum -> LibraryDraft(content.album.title, content.album.subtitle, UUID.randomUUID().toString())
                    is LocalAlbumDialog.RenameSub -> LibraryDraft(content.subAlbums.find { it.localId == dialog.id }?.title ?: return, "", UUID.randomUUID().toString())
                    LocalAlbumDialog.CreateSub, LocalAlbumDialog.None, LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> LibraryDraft("", "", UUID.randomUUID().toString())
                }
                val context = when (val dialog = event.dialog) { LocalAlbumDialog.CreateSub -> "create-sub"; LocalAlbumDialog.EditAlbum -> "edit-album"; is LocalAlbumDialog.RenameSub -> "rename:${dialog.id}"; LocalAlbumDialog.None, LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> "" }
                dispatch(LocalAlbumAction.Open(event.dialog, draft.copy(context = context)))
            }
            LocalAlbumEvent.Dismiss -> { val token = uiState.value.draft.token; requests.cancel(DraftWrite); dispatch(LocalAlbumAction.Dismiss); viewModelScope.launch { runSuspendCatching { drafts.clearIfMatches(draftKey, token) } } }
            is LocalAlbumEvent.Text -> edit(uiState.value.draft.copy(text = event.value, token = UUID.randomUUID().toString()))
            is LocalAlbumEvent.Extra -> edit(uiState.value.draft.copy(extra = event.value, token = UUID.randomUUID().toString()))
            LocalAlbumEvent.Save -> {
                val state = uiState.value; val draft = state.draft
                if (draft.text.isBlank()) return
                val key = when (val dialog = state.dialog) { LocalAlbumDialog.CreateSub -> "create-sub"; LocalAlbumDialog.EditAlbum -> "edit-album"; is LocalAlbumDialog.RenameSub -> "rename:${dialog.id}"; LocalAlbumDialog.None, LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> return }
                write(key, request = {
                    drafts.save(draftKey, draft)
                    when (val dialog = state.dialog) {
                        LocalAlbumDialog.CreateSub -> repository.createSubAlbum(state.id, draft.text, draft.token)
                        LocalAlbumDialog.EditAlbum -> repository.editAlbum(state.id, draft.text, draft.extra)
                        is LocalAlbumDialog.RenameSub -> repository.editSubAlbum(dialog.id, draft.text)
                        LocalAlbumDialog.None, LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> Unit
                    }
                }, success = {
                    dispatch(LocalAlbumAction.Saved(draft.token, state.interaction))
                    runSuspendCatching { drafts.clearIfMatches(draftKey, draft.token) }.onFailure { Log.e("LocalAlbum", "Committed; draft cleanup failed", it) }
                    emitEffect(LocalAlbumEffect.Message("기기에 저장했습니다"))
                })
            }
            LocalAlbumEvent.ConfirmDelete -> {
                val state = uiState.value
                when (val dialog = state.dialog) {
                    LocalAlbumDialog.DeleteAlbum -> write("delete-album", { repository.deleteAlbum(state.id) }, { emitEffect(LocalAlbumEffect.Back) })
                    is LocalAlbumDialog.DeleteSub -> write("delete:${dialog.id}", { repository.deleteSubAlbum(dialog.id) }, { dispatch(LocalAlbumAction.Saved(state.draft.token, state.interaction)) })
                    LocalAlbumDialog.None, LocalAlbumDialog.CreateSub, LocalAlbumDialog.EditAlbum, is LocalAlbumDialog.RenameSub -> Unit
                }
            }
            is LocalAlbumEvent.Cover -> write("cover", { repository.setCover(uiState.value.id, event.uri) }, { emitEffect(LocalAlbumEffect.Message("대표 사진을 저장했습니다")) })
            LocalAlbumEvent.Back -> sendEffect(LocalAlbumEffect.Back)
            LocalAlbumEvent.Connect -> sendEffect(LocalAlbumEffect.Connect)
        }
    }
    private fun edit(draft: LibraryDraft) {
        dispatch(LocalAlbumAction.Edited(draft))
        requests.launchRequest(DraftWrite, RequestPolicy.LatestWins, { runSuspendCatching { drafts.save(draftKey, draft) } }, onSuccess = {}, onFailure = { Log.e("LocalAlbum", "Draft failed", it); emitEffect(LocalAlbumEffect.Message("초안을 저장하지 못했습니다")) })
    }
    private fun write(key: String, request: suspend () -> Unit, success: suspend () -> Unit) {
        requests.launchRequest(Write(key), RequestPolicy.IgnoreWhileRunning, { runSuspendCatching { request() } },
            onStarted = { if (key == "create-sub" || key == "edit-album" || key.startsWith("rename:")) requests.cancel(DraftWrite); dispatch(LocalAlbumAction.Pending(key, true)) }, onSuccess = { success() },
            onFailure = { Log.e("LocalAlbum", "Local write failed", it); emitEffect(LocalAlbumEffect.Message("기기에 저장하지 못했습니다")) },
            onFinished = { dispatch(LocalAlbumAction.Pending(key, false)) })
    }
}
