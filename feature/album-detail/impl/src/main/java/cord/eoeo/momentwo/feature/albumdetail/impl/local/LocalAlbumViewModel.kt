package cord.eoeo.momentwo.feature.albumdetail.impl.local

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.domain.friend.GetFriendListUseCase
import cord.eoeo.momentwo.feature.albumdetail.api.LocalAlbumDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
internal class LocalAlbumViewModel(private val repository: LocalLibraryRepository, private val drafts: LibraryDrafts, id: String, private val accounts: AccountLibraryAccess? = null, private val getFriends: GetFriendListUseCase? = null) :
    ReducerViewModel<LocalAlbumState, LocalAlbumEvent, LocalAlbumAction, LocalAlbumEffect>(LocalAlbumState(id), LocalAlbumReducer) {
    @Inject constructor(repository: LocalLibraryRepository, drafts: LibraryDrafts, accounts: AccountLibraryAccess, getFriends: GetFriendListUseCase, handle: SavedStateHandle) : this(repository, drafts, handle.toRoute<LocalAlbumDetailDestination>().localId,accounts,getFriends)
    private data class Write(val key: String) : RequestKey
    private data object DraftWrite : RequestKey
    private data object ReadAccount : RequestKey
    private data object ReadMembers : RequestKey
    private data object ReadFriends : RequestKey
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
                    if (content == null) {dispatch(LocalAlbumAction.Failed(UiError.NotFound))} else if(content.album.libraryId == "device" || content.album.locallyCreated || content.album.role != null) {
                        dispatch(LocalAlbumAction.Snapshot(content))
                        if (!restored && saved != null) {
                            val dialog = when {
                                saved.context == "create-sub" -> LocalAlbumDialog.CreateSub
                                saved.context == "edit-title" -> LocalAlbumDialog.EditTitle
                                saved.context == "edit-subtitle" -> LocalAlbumDialog.EditSubtitle
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
            is LocalAlbumEvent.Tab -> { dispatch(LocalAlbumAction.Tab(event.index)); if (event.index == 1) loadMembers() else if(event.index == 2) loadPermission() else refreshAccount() }
            LocalAlbumEvent.Edit -> dispatch(LocalAlbumAction.Editing(true))
            LocalAlbumEvent.CancelEdit -> dispatch(LocalAlbumAction.Editing(false))
            is LocalAlbumEvent.Select -> dispatch(LocalAlbumAction.Select(event.id, event.checked))
            is LocalAlbumEvent.SelectMember -> dispatch(LocalAlbumAction.SelectMember(event.id, event.checked))
            LocalAlbumEvent.Add -> if(uiState.value.tab == 0) onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.CreateSub)) else openInvite()
            is LocalAlbumEvent.InviteCheck -> dispatch(LocalAlbumAction.InviteCheck(event.friend, event.checked))
            LocalAlbumEvent.ConfirmInvite -> {
                val names=uiState.value.invited.map { it.nickname };val state=uiState.value
                write("invite", { requireNotNull(accounts).inviteMembers(state.id,names) }, { dispatch(LocalAlbumAction.Saved(state.draft.token,state.interaction));emitEffect(LocalAlbumEffect.Message("친구를 초대했습니다"));loadMembers(force=true) })
            }
            LocalAlbumEvent.OpenCover -> dispatch(LocalAlbumAction.CoverPage(true))
            LocalAlbumEvent.ApplyCover -> if(uiState.value.coverDirty) {
                val state=uiState.value
                write("cover", { repository.setCover(state.id,state.coverDraft?.let(android.net.Uri::parse)) }, { if(uiState.value.interaction==state.interaction)dispatch(LocalAlbumAction.CoverPage(false));emitEffect(LocalAlbumEffect.Message("대표 이미지를 저장했습니다")) })
            }

            LocalAlbumEvent.Retry -> { initialize(); if (uiState.value.tab == 1) loadMembers(force=true) else if(uiState.value.tab==2)loadPermission()else refreshAccount() }
            is LocalAlbumEvent.Open -> {
                val content = (uiState.value.screen as? ScreenState.Success)?.content ?: return
                val draft = when (val dialog = event.dialog) {
                    LocalAlbumDialog.EditTitle -> LibraryDraft(content.album.title, "", UUID.randomUUID().toString())
                    LocalAlbumDialog.EditSubtitle -> LibraryDraft(content.album.subtitle, "", UUID.randomUUID().toString())
                    LocalAlbumDialog.EditAlbum -> LibraryDraft(content.album.title, content.album.subtitle, UUID.randomUUID().toString())
                    is LocalAlbumDialog.RenameSub -> LibraryDraft(content.subAlbums.find { it.localId == dialog.id }?.title ?: return, "", UUID.randomUUID().toString())
                    LocalAlbumDialog.Leave, LocalAlbumDialog.Invite, LocalAlbumDialog.DeleteSelected, LocalAlbumDialog.CreateSub, LocalAlbumDialog.None, LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> LibraryDraft("", "", UUID.randomUUID().toString())
                }
                val context = when (val dialog = event.dialog) { LocalAlbumDialog.CreateSub -> "create-sub"; LocalAlbumDialog.EditTitle -> "edit-title"; LocalAlbumDialog.EditSubtitle -> "edit-subtitle"; LocalAlbumDialog.EditAlbum -> "edit-album"; is LocalAlbumDialog.RenameSub -> "rename:${dialog.id}"; LocalAlbumDialog.Leave, LocalAlbumDialog.Invite, LocalAlbumDialog.DeleteSelected, LocalAlbumDialog.None, LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> "" }
                dispatch(LocalAlbumAction.Open(event.dialog, draft.copy(context = context)))
            }
            LocalAlbumEvent.Dismiss -> { val token = uiState.value.draft.token; requests.cancel(DraftWrite); dispatch(LocalAlbumAction.Dismiss); viewModelScope.launch { runSuspendCatching { drafts.clearIfMatches(draftKey, token) } } }
            is LocalAlbumEvent.Text -> edit(uiState.value.draft.copy(text = event.value, token = UUID.randomUUID().toString()))
            is LocalAlbumEvent.Extra -> edit(uiState.value.draft.copy(extra = event.value, token = UUID.randomUUID().toString()))
            LocalAlbumEvent.Save -> {
                val state = uiState.value; val draft = state.draft
                if (draft.text.isBlank() && state.dialog != LocalAlbumDialog.EditSubtitle) return
                val key = when (val dialog = state.dialog) { LocalAlbumDialog.CreateSub -> "create-sub"; LocalAlbumDialog.EditTitle -> "edit-title"; LocalAlbumDialog.EditSubtitle -> "edit-subtitle"; LocalAlbumDialog.EditAlbum -> "edit-album"; is LocalAlbumDialog.RenameSub -> "rename:${dialog.id}"; LocalAlbumDialog.Leave, LocalAlbumDialog.Invite, LocalAlbumDialog.DeleteSelected, LocalAlbumDialog.None, LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> return }
                write(key, request = {
                    drafts.save(draftKey, draft)
                    when (val dialog = state.dialog) {
                        LocalAlbumDialog.CreateSub -> repository.createSubAlbum(state.id, draft.text, draft.token)
                        LocalAlbumDialog.EditTitle -> repository.editAlbumTitle(state.id, draft.text)
                        LocalAlbumDialog.EditSubtitle -> repository.editAlbumSubtitle(state.id, draft.text)
                        LocalAlbumDialog.EditAlbum -> repository.editAlbum(state.id, draft.text, draft.extra)
                        is LocalAlbumDialog.RenameSub -> repository.editSubAlbum(dialog.id, draft.text)
                        LocalAlbumDialog.Leave, LocalAlbumDialog.Invite, LocalAlbumDialog.DeleteSelected, LocalAlbumDialog.None, LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> Unit
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
                    LocalAlbumDialog.DeleteSelected -> {
                        if (state.tab == 0) write("delete-selected", { repository.deleteSubAlbums(state.id,state.selected) }, { dispatch(LocalAlbumAction.Deleted(state.selected,state.interaction)) })
                        else write("kick", { requireNotNull(accounts).kickMembers(state.id,state.selectedMembers) }, { dispatch(LocalAlbumAction.MembersRemoved(state.selectedMembers,state.interaction));emitEffect(LocalAlbumEffect.Message("선택한 멤버를 추방했습니다"));loadMembers(force=true) })
                    }
                    LocalAlbumDialog.Leave -> write("leave", { requireNotNull(accounts).leaveAlbum(state.id) }, { emitEffect(LocalAlbumEffect.Back) })
                    LocalAlbumDialog.DeleteAlbum -> write("delete-album", { repository.deleteAlbum(state.id) }, { emitEffect(LocalAlbumEffect.Back) })
                    is LocalAlbumDialog.DeleteSub -> write("delete:${dialog.id}", { repository.deleteSubAlbum(dialog.id) }, { dispatch(LocalAlbumAction.Saved(state.draft.token, state.interaction)) })
                    LocalAlbumDialog.Invite, LocalAlbumDialog.None, LocalAlbumDialog.CreateSub, LocalAlbumDialog.EditTitle, LocalAlbumDialog.EditSubtitle, LocalAlbumDialog.EditAlbum, is LocalAlbumDialog.RenameSub -> Unit
                }
            }
            is LocalAlbumEvent.Cover -> dispatch(LocalAlbumAction.CoverDraft(event.uri?.toString()))
            LocalAlbumEvent.Back -> if(uiState.value.coverPage)dispatch(LocalAlbumAction.CoverPage(false))else if(uiState.value.editing)dispatch(LocalAlbumAction.Editing(false))else sendEffect(LocalAlbumEffect.Back)
            LocalAlbumEvent.Connect -> sendEffect(LocalAlbumEffect.Connect)
        }
    }
    private fun loadPermission() {
        val remote=accounts ?: return
        requests.launchRequest(ReadAccount,RequestPolicy.LatestWins,{runSuspendCatching { remote.refreshPermission(uiState.value.id) }},onSuccess={},onFailure={dispatch(LocalAlbumAction.Failed(it.toUiError()))})
    }
    private fun loadMembers(force: Boolean = false) {
        val remote=accounts ?: return
        if(!force && uiState.value.members is ScreenState.Success)return
        requests.launchRequest(ReadMembers,RequestPolicy.LatestWins,{runSuspendCatching {
            val album=repository.album(uiState.value.id).first() ?: error("앨범 없음")
            if(album.libraryId=="device" || album.remoteId==null || album.copying)emptyList()else remote.members(album.localId)
        }},onStarted={dispatch(LocalAlbumAction.Members(uiState.value.members.startRefresh()))},
            onSuccess={dispatch(LocalAlbumAction.Members(ScreenState.Success(it)))},
            onFailure={dispatch(LocalAlbumAction.Members(uiState.value.members.failRefresh(it.toUiError())));if(uiState.value.screen !is ScreenState.Success)dispatch(LocalAlbumAction.Failed(it.toUiError()))})
    }
    private fun openInvite() {
        val album=(uiState.value.screen as? ScreenState.Success)?.content?.album ?: return
        if(album.libraryId=="device" || album.remoteId==null || album.copying){sendEffect(LocalAlbumEffect.Connect);return}
        onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.Invite))
        val remote=accounts ?: return
        requests.launchRequest(ReadFriends,RequestPolicy.LatestWins,{runSuspendCatching {
            val identity=remote.identities().first();check(album.libraryId==identity?.accountId?.let { "account:$it" })
            val rows=requireNotNull(getFriends).invoke().getOrThrow();check(remote.identities().first()==identity);rows
        }},onSuccess={dispatch(LocalAlbumAction.Friends(ScreenState.Success(it)))},onFailure={dispatch(LocalAlbumAction.Friends(ScreenState.Error(it.toUiError())))})
    }
    private fun edit(draft: LibraryDraft) {
        dispatch(LocalAlbumAction.Edited(draft))
        requests.launchRequest(DraftWrite, RequestPolicy.LatestWins, { runSuspendCatching { drafts.save(draftKey, draft) } }, onSuccess = {}, onFailure = { Log.e("LocalAlbum", "Draft failed", it); emitEffect(LocalAlbumEffect.Message("초안을 저장하지 못했습니다")) })
    }
    private fun write(key: String, request: suspend () -> Unit, success: suspend () -> Unit) {
        val library=(uiState.value.screen as? ScreenState.Success)?.content?.album?.libraryId
        suspend fun stillOwned()=library=="device" || (library!=null && library==accounts?.identities()?.first()?.accountId?.let{"account:$it"})
        requests.launchRequest(Write(key), RequestPolicy.IgnoreWhileRunning, { runSuspendCatching { request() } },
            onStarted = { if (key == "create-sub" || key == "edit-album" || key == "edit-title" || key == "edit-subtitle" || key.startsWith("rename:")) requests.cancel(DraftWrite); dispatch(LocalAlbumAction.Pending(key, true)) }, onSuccess = { if(stillOwned())success() },
            onFailure = { Log.e("LocalAlbum", "Local write failed", it); if(stillOwned())emitEffect(LocalAlbumEffect.Message("기기에 저장하지 못했습니다")) },
            onFinished = { dispatch(LocalAlbumAction.Pending(key, false)) })
    }
}
