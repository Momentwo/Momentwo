package cord.eoeo.momentwo.feature.albumdetail.impl

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.album.AlbumRepository
import cord.eoeo.momentwo.core.data.member.MemberRepository
import cord.eoeo.momentwo.core.data.subalbum.SubAlbumRepository
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.domain.friend.GetFriendListUseCase
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.feature.albumdetail.api.AlbumDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

private fun initialState(handle: SavedStateHandle): AlbumDetailState {
    val route = handle.toRoute<AlbumDetailDestination>()
    return AlbumDetailState(AlbumHeader(route.id, route.title, route.subTitle, route.imageUrl), route.imageUrl.takeIf { it.isNotBlank() }?.let { Uri.parse(it) })
}

@HiltViewModel
internal class AlbumDetailViewModel(
    private val getFriendListUseCase: GetFriendListUseCase,
    private val subAlbumRepository: SubAlbumRepository,
    private val memberRepository: MemberRepository,
    private val albumRepository: AlbumRepository,
    initialState: AlbumDetailState,
) : ReducerViewModel<AlbumDetailState, AlbumDetailEvent, AlbumDetailAction, AlbumDetailEffect>(initialState, AlbumDetailReducer) {
    @Inject constructor(
        getFriendListUseCase: GetFriendListUseCase,
        subAlbumRepository: SubAlbumRepository,
        memberRepository: MemberRepository,
        albumRepository: AlbumRepository,
        savedStateHandle: SavedStateHandle,
    ) : this(getFriendListUseCase, subAlbumRepository, memberRepository, albumRepository, initialState(savedStateHandle))

    private data class Read(val tab: DetailTab) : RequestKey
    private data object Friends : RequestKey
    private var initialized = false
    private val albumId get() = uiState.value.albumItem.id

    fun initialize() { if (!initialized) { initialized = true; loadTab(DetailTab.SubAlbums) } }

    override fun handleEvent(event: AlbumDetailEvent) {
        when (event) {
            is AlbumDetailEvent.OnChangeNavIndex -> { dispatch(AlbumDetailAction.NavigationChanged(event.index)); loadTab(uiState.value.tab) }
            is AlbumDetailEvent.OnRetryTab -> loadTab(event.tab, force = true)
            AlbumDetailEvent.OnSubAlbumVisited -> dispatch(SubAlbumAction.Invalidated)
            AlbumDetailEvent.OnGetPermission -> loadTab(uiState.value.tab, force = true)
            AlbumDetailEvent.OnGetFriendList -> loadFriends()
            is AlbumDetailEvent.OnChangeFriendSelected -> dispatch(SelectionAction.Friend(event.isSelected, event.friendItem))
            is AlbumDetailEvent.OnChangeSubAlbumSelected -> dispatch(SelectionAction.SubAlbum(event.isSelected, event.subAlbumId))
            is AlbumDetailEvent.OnChangeMemberSelected -> dispatch(SelectionAction.Member(event.isSelected, event.memberId))
            AlbumDetailEvent.OnClickAdd -> if (uiState.value.permission != null) when (uiState.value.tab) {
                DetailTab.SubAlbums -> dispatch(SettingAction.OpenDialog(AlbumDetailDialog.CreateSubAlbum))
                DetailTab.Members -> openInvite()
                DetailTab.Settings -> Unit
            }
            AlbumDetailEvent.OnClickEdit -> if (uiState.value.permission in listOf(MemberAuth.ADMIN, MemberAuth.SUB_ADMIN)) dispatch(SelectionAction.StartEdit)
            AlbumDetailEvent.OnCancelEdit -> dispatch(SelectionAction.CancelEdit)
            AlbumDetailEvent.OnConfirmEdit -> when (uiState.value.tab) {
                DetailTab.SubAlbums -> subAlbum(SubAlbumEvent.DeleteSubAlbums(uiState.value.selectedSubAlbumIds.toList()))
                DetailTab.Members -> member(MemberEvent.KickMembers(uiState.value.selectedMemberIds.toList()))
                DetailTab.Settings -> Unit
            }
            is AlbumDetailEvent.OnOpenTextFieldDialog -> if (uiState.value.permission != null) dispatch(SettingAction.OpenDialog(event.dialog))
            AlbumDetailEvent.OnDismissTextFieldDialog -> dispatch(SettingAction.OpenDialog(AlbumDetailDialog.None))
            AlbumDetailEvent.OnDialogEdited -> dispatch(SettingAction.DialogEdited)
            AlbumDetailEvent.OnClickInvite -> if (uiState.value.permission != null) openInvite()
            AlbumDetailEvent.OnDismissInviteFriend -> dispatch(MemberAction.DismissInvite)
            AlbumDetailEvent.OnConfirmInviteFriend -> invite()
            is AlbumDetailEvent.OnChangeIsInChangeImage -> dispatch(SettingAction.ImageScreenChanged(event.isInChangeImage))
            is AlbumDetailEvent.OnSelectImage -> dispatch(SettingAction.ImageSelected(event.imageUri))
            AlbumDetailEvent.OnCancelChangeImage -> dispatch(SettingAction.ImageCancelled)
            is AlbumDetailEvent.OnSubAlbumEvents -> subAlbum(event.subAlbumEvents)
            is AlbumDetailEvent.OnMemberEvents -> member(event.memberEvents)
            is AlbumDetailEvent.OnAlbumSettingEvents -> setting(event.albumSettingEvents)
            AlbumDetailEvent.OnBack -> sendEffect(AlbumDetailEffect.PopBackStack)
            AlbumDetailEvent.OnBackInAlbumDetail -> {
                if (uiState.value.isInChangeImage) onEvent(AlbumDetailEvent.OnCancelChangeImage)
                sendEffect(AlbumDetailEffect.PopBackStackInAlbumDetail)
            }
            is AlbumDetailEvent.OnError -> sendEffect(AlbumDetailEffect.ShowSnackbar(event.errorMessage.ifBlank { "작업을 완료하지 못했습니다" }))
        }
    }

    private fun loadTab(tab: DetailTab, force: Boolean = false) {
        val cached = when (tab) { DetailTab.SubAlbums -> uiState.value.subAlbums; DetailTab.Members -> uiState.value.members; DetailTab.Settings -> uiState.value.settings }
        val dirty = tab == DetailTab.SubAlbums && uiState.value.subAlbumsDirty
        if (!force && ((cached is ScreenState.Success && !dirty) || requests.isRunning(Read(tab)))) return
        val version = uiState.value.version(tab)
        requests.launchRequest(Read(tab), RequestPolicy.LatestWins,
            request = { runSuspendCatching { coroutineScope {
                val role = async { albumRepository.getAlbumRole(albumId).getOrThrow() }
                when (tab) {
                    DetailTab.SubAlbums -> { val items = async { subAlbumRepository.getSubAlbumList(albumId).getOrThrow() }; SubAlbumsContent(role.await(), items.await()) }
                    DetailTab.Members -> { val items = async { memberRepository.getMemberList(albumId).getOrThrow() }; MembersContent(role.await(), items.await()) }
                    DetailTab.Settings -> SettingsContent(role.await())
                }
            } } },
            onStarted = { dispatch(when (tab) { DetailTab.SubAlbums -> SubAlbumAction.Started; DetailTab.Members -> MemberAction.Started; DetailTab.Settings -> SettingAction.Started }) },
            onSuccess = { content -> dispatch(when (content) {
                is SubAlbumsContent -> SubAlbumAction.Loaded(content, version)
                is MembersContent -> MemberAction.Loaded(content, version)
                is SettingsContent -> SettingAction.Loaded(content)
            }) },
            onFailure = { Log.e("AlbumDetail", "Tab load failed", it); dispatch(when (tab) {
                DetailTab.SubAlbums -> SubAlbumAction.Failed(it.toUiError())
                DetailTab.Members -> MemberAction.Failed(it.toUiError())
                DetailTab.Settings -> SettingAction.Failed(it.toUiError())
            }) },
            onFinished = { dispatch(when (tab) { DetailTab.SubAlbums -> SubAlbumAction.Finished; DetailTab.Members -> MemberAction.Finished; DetailTab.Settings -> SettingAction.Finished }) })
    }
    private fun openInvite() { dispatch(MemberAction.OpenInvite); loadFriends() }
    private fun loadFriends() {
        if (uiState.value.friends is ScreenState.Success) return
        requests.launchRequest(Friends, RequestPolicy.IgnoreWhileRunning, request = { getFriendListUseCase() },
            onStarted = { dispatch(AlbumDetailAction.FriendsStarted) }, onSuccess = { dispatch(AlbumDetailAction.FriendsLoaded(it)) },
            onFailure = { Log.e("AlbumDetail", "Friend load failed", it); dispatch(AlbumDetailAction.FriendsFailed(it.toUiError())) },
            onFinished = { dispatch(AlbumDetailAction.FriendsFinished) })
    }
    private fun invite() {
        val names = uiState.value.selectedFriendList.map { it.nickname }.toSet()
        val revision = uiState.value.inviteRevision
        if (names.isEmpty()) return
        write(DetailWriteKey(DetailOperation.Invite), DetailTab.Members,
            request = { memberRepository.requestInviteMembers(albumId, names.toList()) },
            success = { dispatch(MemberAction.Invited(names, revision)); loadTab(DetailTab.Members, true) })
    }
    private fun subAlbum(event: SubAlbumEvent) {
        val dialogRevision = uiState.value.dialogRevision
        val editRevision = uiState.value.editRevision
        when (event) {
            SubAlbumEvent.GetSubAlbums -> loadTab(DetailTab.SubAlbums)
            is SubAlbumEvent.Create -> write(DetailWriteKey(DetailOperation.CreateSubAlbum), DetailTab.SubAlbums,
                request = { subAlbumRepository.requestCreateSubAlbum(albumId, event.subAlbumTitle) },
                success = { dispatch(SubAlbumAction.Committed); dispatch(SettingAction.CloseSubmittedDialog(dialogRevision)); loadTab(DetailTab.SubAlbums, true) })
            is SubAlbumEvent.EditTitle -> write(DetailWriteKey(DetailOperation.EditSubAlbumTitle, event.subAlbumId), DetailTab.SubAlbums,
                request = { subAlbumRepository.changeSubAlbumTitle(albumId, event.subAlbumId, event.title) },
                success = { dispatch(SubAlbumAction.Committed); loadTab(DetailTab.SubAlbums, true) })
            is SubAlbumEvent.DeleteSubAlbums -> if (event.subAlbumIds.isNotEmpty()) write(DetailWriteKey(DetailOperation.DeleteSubAlbums), DetailTab.SubAlbums,
                request = { subAlbumRepository.deleteSubAlbums(albumId, event.subAlbumIds) },
                success = { dispatch(SubAlbumAction.Deleted(event.subAlbumIds, editRevision)); loadTab(DetailTab.SubAlbums, true) })
        }
    }
    private fun member(event: MemberEvent) {
        val editRevision = uiState.value.editRevision
        when (event) {
            MemberEvent.GetMembers -> loadTab(DetailTab.Members)
            MemberEvent.Exit -> write(DetailWriteKey(DetailOperation.Exit), request = { memberRepository.exitFromAlbum(albumId) }, success = { emitEffect(AlbumDetailEffect.PopBackStack) })
            is MemberEvent.AssignAdmin -> write(DetailWriteKey(DetailOperation.AssignAdmin), DetailTab.Members,
                request = { memberRepository.assignAdminToMember(albumId, event.nickname) }, success = { dispatch(MemberAction.Committed); loadTab(DetailTab.Members, true) })
            is MemberEvent.EditPermissions -> write(DetailWriteKey(DetailOperation.Permissions), DetailTab.Members,
                request = { memberRepository.editMembersPermission(albumId, event.editMembers) }, success = { dispatch(MemberAction.Committed); loadTab(DetailTab.Members, true) })
            is MemberEvent.KickMembers -> if (event.kickMemberIds.isNotEmpty()) write(DetailWriteKey(DetailOperation.Kick), DetailTab.Members,
                request = { memberRepository.kickMembers(albumId, event.kickMemberIds) }, success = { dispatch(MemberAction.Kicked(event.kickMemberIds, editRevision)); loadTab(DetailTab.Members, true) })
        }
    }
    private fun setting(event: AlbumSettingEvent) {
        val revision = uiState.value.dialogRevision
        when (event) {
            AlbumSettingEvent.DeleteAlbum -> if (uiState.value.permission == MemberAuth.ADMIN) write(DetailWriteKey(DetailOperation.DeleteAlbum),
                request = { albumRepository.deleteAlbum(albumId) }, success = { emitEffect(AlbumDetailEffect.PopBackStack) })
            AlbumSettingEvent.ChangeImage -> {
                val uri = uiState.value.imageUri
                val imageRevision = uiState.value.imageRevision
                write(DetailWriteKey(DetailOperation.Image), request = { if (uri == null) albumRepository.deleteAlbumImage(albumId) else albumRepository.changeAlbumImage(albumId, uri) },
                    success = { dispatch(SettingAction.ImageSaved(uri, imageRevision)); if (uiState.value.imageRevision == imageRevision) emitEffect(AlbumDetailEffect.PopBackStackInAlbumDetail) })
            }
            is AlbumSettingEvent.EditTitle -> write(DetailWriteKey(DetailOperation.Title), request = { albumRepository.changeAlbumTitle(albumId, event.title) },
                success = { dispatch(SettingAction.TitleChanged(event.title, revision)) })
            is AlbumSettingEvent.EditSubTitle -> write(DetailWriteKey(DetailOperation.Subtitle), request = { albumRepository.changeAlbumSubTitle(albumId, event.subTitle) },
                success = { dispatch(SettingAction.SubtitleChanged(event.subTitle, revision)) })
            AlbumSettingEvent.DeleteSubTitle -> write(DetailWriteKey(DetailOperation.Subtitle), request = { albumRepository.deleteAlbumSubTitle(albumId) },
                success = { dispatch(SettingAction.SubtitleChanged("", revision)) })
        }
    }
    private fun write(key: DetailWriteKey, affectedTab: DetailTab? = null, request: suspend () -> Result<Unit>, success: suspend () -> Unit) {
        if (uiState.value.permission == null) return
        requests.launchRequest(key, RequestPolicy.IgnoreWhileRunning, request,
            onStarted = { affectedTab?.let { requests.cancel(Read(it)) }; dispatch(AlbumDetailAction.PendingChanged(key, true)) },
            onSuccess = { success() }, onFailure = { Log.e("AlbumDetail", "Operation failed", it); emitEffect(AlbumDetailEffect.ShowSnackbar("작업을 완료하지 못했습니다")) },
            onFinished = { dispatch(AlbumDetailAction.PendingChanged(key, false)) })
    }
}
