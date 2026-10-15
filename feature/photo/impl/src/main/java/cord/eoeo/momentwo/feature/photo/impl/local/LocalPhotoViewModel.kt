package cord.eoeo.momentwo.feature.photo.impl.local
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.model.LibraryDraft
import cord.eoeo.momentwo.core.model.PhotoDescription
import cord.eoeo.momentwo.feature.photo.api.LocalPhotoDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
@HiltViewModel
internal class LocalPhotoViewModel(private val repository: LocalLibraryRepository, private val drafts: LibraryDrafts, id: String,private val accounts:AccountLibraryAccess?=null,private val sync:AccountSyncOperations?=null) : ReducerViewModel<LocalPhotoState, LocalPhotoEvent, LocalPhotoAction, LocalPhotoEffect>(LocalPhotoState(id),LocalPhotoReducer) {
    @Inject constructor(repository:LocalLibraryRepository,drafts:LibraryDrafts,accounts:AccountLibraryAccess,sync:AccountSyncOperations,handle:SavedStateHandle):this(repository,drafts,handle.toRoute<LocalPhotoDetailDestination>().localId,accounts,sync)
    private data class Write(val key: String) : RequestKey
    private data object DraftWrite : RequestKey
    private data object ReadPhoto:RequestKey
    private data object ReadComments:RequestKey
    private val draftKey = "device:photo:$id"
    private var observation: Job? = null
    fun initialize() {
        if(observation?.isActive == true) return
        refreshPhoto()
        observation = viewModelScope.launch {
            val saved = runSuspendCatching { drafts.load(draftKey) }.getOrElse { Log.e("LocalPhoto", "Draft restore failed", it); null }
            var restored = false
            combine(repository.photo(uiState.value.id),repository.memos(uiState.value.id),sync?.comments(uiState.value.id) ?: flowOf(emptyList())) {photo,memos,comments->photo?.let{LocalPhotoContent(it,memos,if(it.commentsLoaded)ScreenState.Success(comments)else ScreenState.Loading)}}
            .catch { if(it is CancellationException || it is Error) throw it; Log.e("LocalPhoto", "Observation failed", it); dispatch(LocalPhotoAction.Failed(UiError.Unknown)) }
            .collect { content -> if(content == null) {dispatch(LocalPhotoAction.Failed(UiError.NotFound))} else {
                dispatch(LocalPhotoAction.Snapshot(content))
                if(!restored && saved != null) {
                    val dialog = when { saved.context in setOf("caption","caption:v2") -> LocalPhotoDialog.Caption; saved.context == "comment:new" -> LocalPhotoDialog.Comment(null); saved.context.startsWith("comment:") && (content.comments as? ScreenState.Success)?.content?.any{it.localId==saved.context.removePrefix("comment:")}==true -> LocalPhotoDialog.Comment(saved.context.removePrefix("comment:")); saved.context == "memo:new" -> LocalPhotoDialog.Memo(null); saved.context.startsWith("memo:") && content.memos.any { it.localId == saved.context.removePrefix("memo:") } -> LocalPhotoDialog.Memo(saved.context.removePrefix("memo:")); else -> LocalPhotoDialog.None }
                    if(dialog != LocalPhotoDialog.None) dispatch(LocalPhotoAction.Restored(dialog,if(saved.context=="caption")PhotoDescription.read(saved.text).let { saved.copy(text=it.title,extra=it.body,context="caption:v2") }else saved))
                }; restored = true
            } }
        }
    }
    private fun refreshPhoto(){val remote=accounts ?: return;requests.launchRequest(ReadPhoto,RequestPolicy.LatestWins,{runSuspendCatching{remote.refreshPhoto(uiState.value.id)}},onSuccess={},onFailure={Log.e("LocalPhoto","Account details could not refresh",it);val current=(uiState.value.screen as? ScreenState.Success)?.content?.photo;if(current?.libraryId!="device")dispatch(LocalPhotoAction.Failed(it.toUiError()))})}
    private fun refreshComments(){val remote=accounts ?: return;requests.launchRequest(ReadComments,RequestPolicy.LatestWins,{runSuspendCatching{remote.refreshComments(uiState.value.id)}},onSuccess={},onFailure={Log.e("LocalPhoto","Comments could not refresh",it);dispatch(LocalPhotoAction.CommentFailed(it.toUiError()))})}
    override fun handleEvent(event: LocalPhotoEvent) {
        when(event) {
            is LocalPhotoEvent.Sheet -> { if(event.sheet==LocalPhotoSheet.None)onEvent(LocalPhotoEvent.Dismiss);dispatch(LocalPhotoAction.Sheet(event.sheet)) }
            is LocalPhotoEvent.Body -> edit(uiState.value.draft.copy(extra=event.text,token=UUID.randomUUID().toString()))
            LocalPhotoEvent.DeleteDescription -> write("caption",{repository.setDescription(uiState.value.id,"")},{dispatch(LocalPhotoAction.Dismiss)})

            LocalPhotoEvent.Retry -> {initialize();refreshPhoto()}
            LocalPhotoEvent.Refresh->refreshPhoto()
            LocalPhotoEvent.OpenComments->{dispatch(LocalPhotoAction.OpenComments);refreshComments()}
            LocalPhotoEvent.Like->{val photo=(uiState.value.screen as? ScreenState.Success)?.content?.photo ?: return;val queue=sync ?: return;write("like",{queue.setLiked(photo.localId,!photo.isLiked)},{})}
            is LocalPhotoEvent.DeleteComment->{val queue=sync ?: return;write("delete-comment:${event.id}",{queue.deleteComment(event.id)},{})}
            LocalPhotoEvent.Back -> sendEffect(LocalPhotoEffect.Back)
            LocalPhotoEvent.ToggleImmersive -> dispatch(LocalPhotoAction.ToggleImmersive)
            is LocalPhotoEvent.Open -> {
                val content = (uiState.value.screen as? ScreenState.Success)?.content ?: return
                val text = when(val dialog = event.dialog) { LocalPhotoDialog.Caption -> PhotoDescription.read(content.photo.description).title; is LocalPhotoDialog.Memo -> if(dialog.id == null) "" else content.memos.find { it.localId == dialog.id }?.text ?: return; is LocalPhotoDialog.Comment->if(dialog.id==null)"" else (content.comments as? ScreenState.Success)?.content?.find{it.localId==dialog.id}?.text ?: return; LocalPhotoDialog.None, LocalPhotoDialog.Delete -> "" }
                val context = when(val dialog = event.dialog) { LocalPhotoDialog.Caption -> "caption:v2"; is LocalPhotoDialog.Memo -> "memo:${dialog.id ?: "new"}";is LocalPhotoDialog.Comment->"comment:${dialog.id ?: "new"}"; LocalPhotoDialog.None, LocalPhotoDialog.Delete -> "" }
                dispatch(LocalPhotoAction.Open(event.dialog,LibraryDraft(text,if(event.dialog==LocalPhotoDialog.Caption)PhotoDescription.read(content.photo.description).body else "",UUID.randomUUID().toString(),context)))
            }
            LocalPhotoEvent.Dismiss -> { val token = uiState.value.draft.token; requests.cancel(DraftWrite); dispatch(LocalPhotoAction.Dismiss); viewModelScope.launch { runSuspendCatching { drafts.clearIfMatches(draftKey,token) } } }
            is LocalPhotoEvent.Text -> edit(uiState.value.draft.copy(text=event.value,token=UUID.randomUUID().toString()))
            LocalPhotoEvent.Save -> {
                val state = uiState.value; val draft = state.draft
                val key = when(val dialog = state.dialog) { LocalPhotoDialog.Caption -> "caption"; is LocalPhotoDialog.Memo -> "memo:${dialog.id ?: "new"}";is LocalPhotoDialog.Comment->"comment:${dialog.id ?: "new"}"; LocalPhotoDialog.None,LocalPhotoDialog.Delete -> return }
                if((state.dialog is LocalPhotoDialog.Memo || state.dialog is LocalPhotoDialog.Comment) && draft.text.isBlank()) return
                write(key,{ drafts.save(draftKey,draft); when(val dialog = state.dialog) { LocalPhotoDialog.Caption -> repository.setDescription(state.id,PhotoDescription(draft.text,draft.extra).encode()); is LocalPhotoDialog.Memo -> repository.saveMemo(state.id,draft.text,dialog.id ?: draft.token); is LocalPhotoDialog.Comment->requireNotNull(sync).saveComment(state.id,draft.text,dialog.id ?: draft.token);LocalPhotoDialog.None,LocalPhotoDialog.Delete -> Unit } },{
                    dispatch(LocalPhotoAction.Saved(draft.token,state.interaction)); runSuspendCatching {drafts.clearIfMatches(draftKey,draft.token)}.onFailure {Log.e("LocalPhoto","Saved; draft cleanup failed",it)}; emitEffect(LocalPhotoEffect.Message("기기에 저장했습니다"))
                })
            }
            LocalPhotoEvent.Favorite -> { val photo = (uiState.value.screen as? ScreenState.Success)?.content?.photo ?: return; write("favorite",{repository.setFavorite(photo.localId,!photo.favorite)},{}) }
            LocalPhotoEvent.Export -> write("export",{repository.exportPhoto(uiState.value.id)},{emitEffect(LocalPhotoEffect.Message("기기 갤러리에 내보냈습니다"))})
            LocalPhotoEvent.ConfirmDelete -> if(uiState.value.dialog == LocalPhotoDialog.Delete) write("delete",{repository.deletePhoto(uiState.value.id)},{emitEffect(LocalPhotoEffect.Back)})
            is LocalPhotoEvent.DeleteMemo -> write("delete-memo:${event.id}",{repository.deleteMemo(event.id)},{})
        }
    }
    private fun edit(draft: LibraryDraft) {
        dispatch(LocalPhotoAction.Edited(draft))
        requests.launchRequest(DraftWrite,RequestPolicy.LatestWins,{runSuspendCatching{drafts.save(draftKey,draft)}},onSuccess={},onFailure={Log.e("LocalPhoto","Draft failed",it);emitEffect(LocalPhotoEffect.Message("초안을 저장하지 못했습니다"))})
    }
    private fun write(key: String, request: suspend () -> Unit, success: suspend () -> Unit) {
        val library=(uiState.value.screen as? ScreenState.Success)?.content?.photo?.libraryId
        suspend fun stillOwned()=library=="device" || (library!=null && library==accounts?.identities()?.first()?.accountId?.let{"account:$it"})
        requests.launchRequest(Write(key),RequestPolicy.IgnoreWhileRunning,{runSuspendCatching {request()}},onStarted={if(key=="caption" || key.startsWith("memo:") || key.startsWith("comment:")) requests.cancel(DraftWrite);dispatch(LocalPhotoAction.Pending(key,true))},onSuccess={if(stillOwned())success()},onFailure={Log.e("LocalPhoto","Local write failed",it);if(stillOwned())emitEffect(LocalPhotoEffect.Message("기기에 저장하지 못했습니다"))},onFinished={dispatch(LocalPhotoAction.Pending(key,false))})
    }
}
