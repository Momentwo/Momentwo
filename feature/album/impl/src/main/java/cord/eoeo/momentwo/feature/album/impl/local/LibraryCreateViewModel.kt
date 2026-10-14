package cord.eoeo.momentwo.feature.album.impl.local
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.domain.friend.GetFriendListUseCase
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.feature.album.api.LibraryCreateAlbumDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@HiltViewModel
internal class LibraryCreateViewModel(
    private val local: LocalLibraryRepository, private val drafts: LibraryDrafts, account: Boolean = false,
    private val accounts: AccountLibraryRepository? = null, private val sync: AccountSyncRepository? = null, private val friends: GetFriendListUseCase? = null,
) : ReducerViewModel<LibraryCreateState, LibraryCreateEvent, LibraryCreateAction, LibraryCreateEffect>(
    LibraryCreateState(account = account), LibraryCreateReducer) {
    @Inject constructor(handle: SavedStateHandle, local: LocalLibraryRepository, accounts: AccountLibraryRepository, sync: AccountSyncRepository, drafts: LibraryDrafts, friends: GetFriendListUseCase): this(local,drafts,handle.toRoute<LibraryCreateAlbumDestination>().account,accounts,sync,friends)
    private data object Save : RequestKey
    private data object Draft : RequestKey
    private data object Friends : RequestKey
    private var initialized = false
    private var key = "device:create-album"
    fun initialize() {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            runSuspendCatching {
                local.initialize()
                val identity = if (uiState.value.account) requireNotNull(accounts).identities().first() else null
                if (uiState.value.account) requireNotNull(identity) { "계정을 연결해주세요" }
                key = if (identity == null) "device:create-album" else "${LibraryScope.Account(identity.accountId).id}:create-album"
                val draft = drafts.load(key) ?: LibraryDraft("", "", UUID.randomUUID().toString())
                dispatch(LibraryCreateAction.Restore(draft, identity, AlbumInvitations.decode(draft.extra)))
                if(uiState.value.account)requireNotNull(accounts).identities().collect { dispatch(LibraryCreateAction.Owner(it)) }
            }.onFailure { initialized=false; dispatch(LibraryCreateAction.RestoreFailed);Log.e("LibraryCreate", "Restore failed", it); emitEffect(LibraryCreateEffect.Message("계정 또는 초안을 확인해주세요")) }
        }
    }
    override fun handleEvent(event: LibraryCreateEvent) { when (event) {
        LibraryCreateEvent.Retry -> initialize()
        LibraryCreateEvent.Back -> sendEffect(LibraryCreateEffect.Back)
        is LibraryCreateEvent.Text -> edit(event.text, uiState.value.selected)
        is LibraryCreateEvent.Toggle -> {
            val names = uiState.value.selected
            edit(uiState.value.draft.text, if (event.name in names) names - event.name else names + event.name)
        }
        is LibraryCreateEvent.Check -> dispatch(LibraryCreateAction.Check(event.name, event.checked))
        LibraryCreateEvent.ConfirmInvite -> { edit(uiState.value.draft.text, uiState.value.temporary); dispatch(LibraryCreateAction.Invite(false)) }
        LibraryCreateEvent.DismissInvite -> dispatch(LibraryCreateAction.Invite(false))
        LibraryCreateEvent.Invite -> if (!uiState.value.account) sendEffect(LibraryCreateEffect.Connect) else {
            dispatch(LibraryCreateAction.Invite(true))
            val expected = uiState.value.identity ?: return
            requests.launchRequest(Friends, RequestPolicy.LatestWins, { runSuspendCatching {
                check(requireNotNull(accounts).identities().first() == expected)
                val rows = requireNotNull(friends).invoke().getOrThrow()
                check(requireNotNull(accounts).identities().first() == expected)
                rows
            } }, onSuccess = { dispatch(LibraryCreateAction.Friends(it)) }, onFailure = { dispatch(LibraryCreateAction.FriendsFailed) })
        }
        LibraryCreateEvent.Submit -> submit()
    } }
    private fun edit(text: String, names: List<String>) {
        if (!uiState.value.ready || !uiState.value.ownerAvailable) return
        val draft = LibraryDraft(text, AlbumInvitations.encode(names), UUID.randomUUID().toString())
        dispatch(LibraryCreateAction.Edit(draft, names))
        requests.launchRequest(Draft, RequestPolicy.LatestWins, { runSuspendCatching { drafts.save(key, draft) } },
            onSuccess = {}, onFailure = { emitEffect(LibraryCreateEffect.Message("초안을 저장하지 못했습니다")) })
    }
    private fun submit() {
        val snapshot = uiState.value
        if (!snapshot.ready || !snapshot.ownerAvailable || snapshot.draft.text.isBlank()) return
        requests.launchRequest(Save, RequestPolicy.IgnoreWhileRunning, { runSuspendCatching {
            drafts.save(key, snapshot.draft)
            if (snapshot.account) requireNotNull(sync).createAccountAlbum(snapshot.draft.text, snapshot.draft.token, requireNotNull(snapshot.identity), snapshot.selected)
            else local.createAlbum(snapshot.draft.text, snapshot.draft.token)
        } }, onStarted = { requests.cancel(Draft); dispatch(LibraryCreateAction.Pending(true)) },
            onSuccess = {
                runSuspendCatching { drafts.clearIfMatches(key, snapshot.draft.token) }.onFailure { Log.e("LibraryCreate", "Draft cleanup deferred", it) }
                if (snapshot.account && requireNotNull(accounts).identities().first() != snapshot.identity) return@launchRequest
                if (uiState.value.revision == snapshot.revision) { dispatch(LibraryCreateAction.Reset(snapshot.draft.token)); emitEffect(LibraryCreateEffect.Created(snapshot.account)) }
                else emitEffect(LibraryCreateEffect.Message("요청한 앨범을 저장했습니다"))
            }, onFailure = { Log.e("LibraryCreate", "Create failed", it); emitEffect(LibraryCreateEffect.Message("앨범을 저장하지 못했습니다")) },
            onFinished = { dispatch(LibraryCreateAction.Pending(false)) })
    }
}
