package cord.eoeo.momentwo.feature.friend.impl

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.friend.FriendRepository
import cord.eoeo.momentwo.core.data.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class FriendViewModel @Inject constructor(private val friendRepository: FriendRepository) :
    ReducerViewModel<FriendState, FriendEvent, FriendAction, FriendEffect>(FriendState(), FriendReducer) {
    private data object PagingSource : RequestKey
    private data object Search : RequestKey
    private data class ReadRequests(val tab: RequestTab) : RequestKey
    private var initialized = false

    fun initialize() {
        if (initialized && uiState.value.friendPagingSource is ScreenState.Success) return
        initialized = true
        requests.launchRequest(PagingSource, RequestPolicy.IgnoreWhileRunning,
            request = { Result.success(friendRepository.getFriendPage(10).cachedIn(viewModelScope)) },
            onSuccess = { dispatch(FriendAction.PagingReady(it)) },
            onFailure = { Log.e("Friend", "Paging source failed", it); dispatch(FriendAction.PagingFailed(it.toUiError())) })
    }

    override fun handleEvent(event: FriendEvent) {
        when (event) {
            is FriendEvent.OnChangeNavIndex -> {
                dispatch(FriendAction.NavigationChanged(event.navIndex))
                if (event.navIndex == 1) loadRequests(uiState.value.requestTab)
            }
            is FriendEvent.OnSelectRequestTab -> {
                dispatch(FriendAction.RequestTabChanged(event.tab)); loadRequests(event.tab)
            }
            FriendEvent.OnClickRequestFriend -> dispatch(FriendAction.DialogChanged(true))
            FriendEvent.OnDismissRequestFriend -> { requests.cancel(Search); dispatch(FriendAction.DialogChanged(false)) }
            FriendEvent.OnRequestDialogEdited -> dispatch(FriendAction.DialogEdited)
            is FriendEvent.OnSearchQueryChanged -> { requests.cancel(Search); dispatch(FriendAction.SearchQueryChanged(event.query)) }
            is FriendEvent.OnGetSearchUser -> search(event.query)
            FriendEvent.OnGetReceivedRequests -> loadRequests(RequestTab.Received, force = true)
            FriendEvent.OnGetSentRequests -> loadRequests(RequestTab.Sent, force = true)
            is FriendEvent.OnSendFriendRequest -> {
                val revision = uiState.value.dialogRevision
                write(FriendWriteKey.Send(event.nickname), "친구 요청을 보내지 못했습니다.",
                    request = { friendRepository.sendFriendRequest(event.nickname) },
                    before = { requests.cancel(ReadRequests(RequestTab.Sent)) },
                    success = { dispatch(FriendAction.RequestSent(revision)); refreshVisibleRequests(RequestTab.Sent) })
            }
            is FriendEvent.OnResponseFriendRequest -> write(FriendWriteKey.Respond(event.userId), "친구 요청에 대한 작업을 수행하지 못했습니다.",
                request = { friendRepository.responseFriendRequest(event.nickname, event.isAccepted) },
                before = { requests.cancel(ReadRequests(RequestTab.Received)) },
                success = { dispatch(FriendAction.Responded(event.userId, event.isAccepted)); refreshVisibleRequests(RequestTab.Received) })
            is FriendEvent.OnCancelFriendRequest -> write(FriendWriteKey.Cancel(event.userId), "친구 요청을 취소하지 못했습니다.",
                request = { friendRepository.cancelFriendRequest(event.userId) },
                before = { requests.cancel(ReadRequests(RequestTab.Sent)) },
                success = { dispatch(FriendAction.Cancelled(event.userId)); refreshVisibleRequests(RequestTab.Sent) })
            FriendEvent.OnBack -> sendEffect(FriendEffect.PopBackStack)
            is FriendEvent.OnError -> sendEffect(FriendEffect.ShowSnackbar(event.errorMessage))
        }
    }

    private fun refreshVisibleRequests(tab: RequestTab) {
        if (uiState.value.selectedNavIndex == 1 && uiState.value.requestTab == tab) loadRequests(tab, force = true)
    }
    private fun loadRequests(tab: RequestTab, force: Boolean = false) {
        if (!force && uiState.value.requests(tab) is ScreenState.Success && !uiState.value.dirty(tab)) return
        if (!force && requests.isRunning(ReadRequests(tab))) return
        val version = uiState.value.version(tab)
        requests.launchRequest(ReadRequests(tab), RequestPolicy.LatestWins,
            request = { when (tab) {
                RequestTab.Received -> friendRepository.getReceivedRequests()
                RequestTab.Sent -> friendRepository.getSentRequests()
            }.map { FriendRequestsContent(it) } },
            onStarted = { dispatch(FriendAction.RequestsStarted(tab)) },
            onSuccess = { dispatch(FriendAction.RequestsLoaded(tab, it, version)) },
            onFailure = { Log.e("Friend", "Request list failed", it); dispatch(FriendAction.RequestsFailed(tab, it.toUiError())) },
            onFinished = { dispatch(FriendAction.RequestsFinished(tab)) })
    }
    private fun search(query: String) {
        if (query.isBlank() || query != uiState.value.searchQuery) return
        requests.launchRequest(Search, RequestPolicy.LatestWins,
            request = { Result.success(friendRepository.getSearchUsers(query, 10).cachedIn(viewModelScope)) },
            onSuccess = { dispatch(FriendAction.SearchReady(query, it)) },
            onFailure = { Log.e("Friend", "User search failed", it); emitEffect(FriendEffect.ShowSnackbar("사용자를 검색하지 못했습니다.")) })
    }
    private fun write(key: FriendWriteKey, message: String, request: suspend () -> Result<Unit>, before: () -> Unit, success: suspend () -> Unit) {
        requests.launchRequest(key, RequestPolicy.IgnoreWhileRunning, request,
            onStarted = { before(); dispatch(FriendAction.PendingChanged(key, true)) },
            onSuccess = { success() },
            onFailure = { Log.e("Friend", "Friend operation failed", it); emitEffect(FriendEffect.ShowSnackbar(message)) },
            onFinished = { dispatch(FriendAction.PendingChanged(key, false)) })
    }
}
