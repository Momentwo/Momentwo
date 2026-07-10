package cord.eoeo.momentwo.ui.friend

import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.domain.friend.FriendRepository
import cord.eoeo.momentwo.core.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel
    @Inject
    constructor(
        private val friendRepository: FriendRepository,
    ) : BaseViewModel<FriendContract.State, FriendContract.Event, FriendContract.Effect>() {
        init {
            viewModelScope.launch {
                setState(
                    uiState.value.copy(
                        friendPagingData = friendRepository.getFriendPage(10),
                    ),
                )
            }
        }

        override fun createInitialState(): FriendContract.State = FriendContract.State()

        override fun handleEvent(newEvent: FriendContract.Event) {
            when (newEvent) {
                is FriendContract.Event.OnChangeNavIndex -> {
                    with(uiState.value) { setState(copy(selectedNavIndex = newEvent.navIndex)) }
                }

                is FriendContract.Event.OnClickRequestFriend -> {
                    with(uiState.value) { setState(copy(isRequestDialogOpened = true)) }
                }

                is FriendContract.Event.OnDismissRequestFriend -> {
                    with(uiState.value) {
                        setState(
                            copy(
                                isRequestDialogOpened = false,
                                searchUserPagingData = emptyFlow(),
                            ),
                        )
                    }
                }

                is FriendContract.Event.OnSendFriendRequest -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            friendRepository
                                .sendFriendRequest(newEvent.nickname)
                                .onSuccess {
                                    setState(
                                        copy(
                                            isRequestDialogOpened = false,
                                            isSentListChanged = true,
                                            searchUserPagingData = emptyFlow(),
                                        ),
                                    )
                                }.onFailure { exception ->
                                    // TODO: 오류 표현
                                    Log.e("Friend", "sendFriendRequest Failure", exception)
                                    setEvent(FriendContract.Event.OnError("친구 요청을 보내지 못했습니다."))
                                }
                        }
                    }
                }

                is FriendContract.Event.OnResponseFriendRequest -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            friendRepository
                                .responseFriendRequest(newEvent.nickname, newEvent.isAccepted)
                                .onSuccess {
                                    val newReceivedRequests = receivedRequests.toMutableList()

                                    newReceivedRequests[newEvent.itemIndex] =
                                        receivedRequests[newEvent.itemIndex].copy(isUpdated = true)

                                    setState(
                                        copy(
                                            receivedRequests = newReceivedRequests,
                                            isFriendListChanged = newEvent.isAccepted,
                                            isReceivedListChanged = true,
                                        ),
                                    )
                                }.onFailure { exception ->
                                    // TODO: 오류 표현
                                    Log.e("Friend", "responseFriendRequest Failure", exception)
                                    setEvent(FriendContract.Event.OnError("친구 요청에 대한 작업을 수행하지 못했습니다."))
                                }
                        }
                    }
                }

                is FriendContract.Event.OnCancelFriendRequest -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            friendRepository
                                .cancelFriendRequest(newEvent.userId)
                                .onSuccess {
                                    val newSentRequests = sentRequests.toMutableList()

                                    newSentRequests[newEvent.itemIndex] =
                                        sentRequests[newEvent.itemIndex].copy(isUpdated = true)

                                    setState(copy(sentRequests = newSentRequests, isSentListChanged = true))
                                }.onFailure { exception ->
                                    // TODO: 오류 표현
                                    Log.e("Friend", "cancelFriendRequest Failure", exception)
                                    setEvent(FriendContract.Event.OnError("친구 요청을 취소하지 못했습니다."))
                                }
                        }
                    }
                }

                is FriendContract.Event.OnGetReceivedRequests -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            if (isReceivedListChanged.not()) return@launch
                            friendRepository
                                .getReceivedRequests()
                                .onSuccess { receivedRequests ->
                                    setState(copy(receivedRequests = receivedRequests, isReceivedListChanged = false))
                                }.onFailure { exception ->
                                    // TODO: 오류 표현
                                    Log.e("Friend", "getReceivedRequests Failure", exception)
                                    setEvent(FriendContract.Event.OnError("받은 친구 요청 목록을 불러오지 못했습니다."))
                                }
                        }
                    }
                }

                is FriendContract.Event.OnGetSentRequests -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            if (isSentListChanged.not()) return@launch
                            friendRepository
                                .getSentRequests()
                                .onSuccess { sentRequests ->
                                    setState(copy(sentRequests = sentRequests, isSentListChanged = false))
                                }.onFailure { exception ->
                                    // TODO: 오류 표현
                                    Log.e("Friend", "getSentRequests Failure", exception)
                                    setEvent(FriendContract.Event.OnError("보낸 친구 요청 목록을 불러오지 못했습니다."))
                                }
                        }
                    }
                }

                is FriendContract.Event.OnGetSearchUser -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            setState(copy(searchUserPagingData = friendRepository.getSearchUsers(newEvent.query, 10)))
                        }
                    }
                }

                is FriendContract.Event.OnBack -> {
                    setEffect { FriendContract.Effect.PopBackStack }
                }

                is FriendContract.Event.OnError -> {
                    setEffect { FriendContract.Effect.ShowSnackbar(newEvent.errorMessage) }
                }
            }
        }
    }
