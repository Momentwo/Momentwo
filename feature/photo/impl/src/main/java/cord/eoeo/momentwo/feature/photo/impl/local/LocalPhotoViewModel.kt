package cord.eoeo.momentwo.feature.photo.impl.local
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.model.LibraryDraft
import cord.eoeo.momentwo.feature.photo.api.LocalPhotoDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
@HiltViewModel
internal class LocalPhotoViewModel(private val repository: LocalLibraryRepository, private val drafts: LibraryDrafts, id: String) : ReducerViewModel<LocalPhotoState, LocalPhotoEvent, LocalPhotoAction, LocalPhotoEffect>(LocalPhotoState(id),LocalPhotoReducer) {
    @Inject constructor(repository: LocalLibraryRepository, drafts: LibraryDrafts, handle: SavedStateHandle) : this(repository,drafts,handle.toRoute<LocalPhotoDetailDestination>().localId)
    private data class Write(val key: String) : RequestKey
    private data object DraftWrite : RequestKey
    private val draftKey = "device:photo:$id"
    private var observation: Job? = null
    fun initialize() {
        if(observation?.isActive == true) return
        observation = viewModelScope.launch {
            val saved = runSuspendCatching { drafts.load(draftKey) }.getOrElse { Log.e("LocalPhoto", "Draft restore failed", it); null }
            var restored = false
            combine(repository.photo(uiState.value.id),repository.memos(uiState.value.id)) { photo,memos -> photo?.let { LocalPhotoContent(it,memos) } }
            .catch { if(it is CancellationException || it is Error) throw it; Log.e("LocalPhoto", "Observation failed", it); dispatch(LocalPhotoAction.Failed(UiError.Unknown)) }
            .collect { content -> if(content == null) dispatch(LocalPhotoAction.Failed(UiError.NotFound)) else {
                dispatch(LocalPhotoAction.Snapshot(content))
                if(!restored && saved != null) {
                    val dialog = when { saved.context == "caption" -> LocalPhotoDialog.Caption; saved.context == "memo:new" -> LocalPhotoDialog.Memo(null); saved.context.startsWith("memo:") && content.memos.any { it.localId == saved.context.removePrefix("memo:") } -> LocalPhotoDialog.Memo(saved.context.removePrefix("memo:")); else -> LocalPhotoDialog.None }
                    if(dialog != LocalPhotoDialog.None) dispatch(LocalPhotoAction.Restored(dialog,saved))
                }; restored = true
            } }
        }
    }
    override fun handleEvent(event: LocalPhotoEvent) {
        when(event) {
            LocalPhotoEvent.Retry -> initialize()
            LocalPhotoEvent.Back -> sendEffect(LocalPhotoEffect.Back)
            LocalPhotoEvent.ToggleImmersive -> dispatch(LocalPhotoAction.ToggleImmersive)
            is LocalPhotoEvent.Open -> {
                val content = (uiState.value.screen as? ScreenState.Success)?.content ?: return
                val text = when(val dialog = event.dialog) { LocalPhotoDialog.Caption -> content.photo.description; is LocalPhotoDialog.Memo -> if(dialog.id == null) "" else content.memos.find { it.localId == dialog.id }?.text ?: return; LocalPhotoDialog.None, LocalPhotoDialog.Delete -> "" }
                val context = when(val dialog = event.dialog) { LocalPhotoDialog.Caption -> "caption"; is LocalPhotoDialog.Memo -> "memo:${dialog.id ?: "new"}"; LocalPhotoDialog.None, LocalPhotoDialog.Delete -> "" }
                dispatch(LocalPhotoAction.Open(event.dialog,LibraryDraft(text,"",UUID.randomUUID().toString(),context)))
            }
            LocalPhotoEvent.Dismiss -> { val token = uiState.value.draft.token; requests.cancel(DraftWrite); dispatch(LocalPhotoAction.Dismiss); viewModelScope.launch { runSuspendCatching { drafts.clearIfMatches(draftKey,token) } } }
            is LocalPhotoEvent.Text -> {
                val draft = uiState.value.draft.copy(text = event.value,token = UUID.randomUUID().toString()); dispatch(LocalPhotoAction.Edited(draft))
                requests.launchRequest(DraftWrite,RequestPolicy.LatestWins,{runSuspendCatching {drafts.save(draftKey,draft)}},onSuccess={},onFailure={Log.e("LocalPhoto","Draft failed",it);emitEffect(LocalPhotoEffect.Message("초안을 저장하지 못했습니다"))})
            }
            LocalPhotoEvent.Save -> {
                val state = uiState.value; val draft = state.draft
                val key = when(val dialog = state.dialog) { LocalPhotoDialog.Caption -> "caption"; is LocalPhotoDialog.Memo -> "memo:${dialog.id ?: "new"}"; LocalPhotoDialog.None,LocalPhotoDialog.Delete -> return }
                if(state.dialog is LocalPhotoDialog.Memo && draft.text.isBlank()) return
                write(key,{ drafts.save(draftKey,draft); when(val dialog = state.dialog) { LocalPhotoDialog.Caption -> repository.setDescription(state.id,draft.text); is LocalPhotoDialog.Memo -> repository.saveMemo(state.id,draft.text,dialog.id ?: draft.token); LocalPhotoDialog.None,LocalPhotoDialog.Delete -> Unit } },{
                    dispatch(LocalPhotoAction.Saved(draft.token,state.interaction)); runSuspendCatching {drafts.clearIfMatches(draftKey,draft.token)}.onFailure {Log.e("LocalPhoto","Saved; draft cleanup failed",it)}; emitEffect(LocalPhotoEffect.Message("기기에 저장했습니다"))
                })
            }
            LocalPhotoEvent.Favorite -> { val photo = (uiState.value.screen as? ScreenState.Success)?.content?.photo ?: return; write("favorite",{repository.setFavorite(photo.localId,!photo.favorite)},{}) }
            LocalPhotoEvent.Export -> write("export",{repository.exportPhoto(uiState.value.id)},{emitEffect(LocalPhotoEffect.Message("기기 갤러리에 내보냈습니다"))})
            LocalPhotoEvent.ConfirmDelete -> if(uiState.value.dialog == LocalPhotoDialog.Delete) write("delete",{repository.deletePhoto(uiState.value.id)},{emitEffect(LocalPhotoEffect.Back)})
            is LocalPhotoEvent.DeleteMemo -> write("delete-memo:${event.id}",{repository.deleteMemo(event.id)},{})
        }
    }
    private fun write(key: String, request: suspend () -> Unit, success: suspend () -> Unit) {
        requests.launchRequest(Write(key),RequestPolicy.IgnoreWhileRunning,{runSuspendCatching {request()}},onStarted={if(key=="caption" || key.startsWith("memo:")) requests.cancel(DraftWrite);dispatch(LocalPhotoAction.Pending(key,true))},onSuccess={success()},onFailure={Log.e("LocalPhoto","Local write failed",it);emitEffect(LocalPhotoEffect.Message("기기에 저장하지 못했습니다"))},onFinished={dispatch(LocalPhotoAction.Pending(key,false))})
    }
}
