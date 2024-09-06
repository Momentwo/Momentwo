package cord.eoeo.momentwo.ui.friend

import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.data.friend.FriendRepository
import cord.eoeo.momentwo.ui.BaseViewModel
import cord.eoeo.momentwo.ui.model.FriendRequestItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel
    @Inject
    constructor(
        private val friendRepository: FriendRepository,
    ) : BaseViewModel<FriendContract.State, FriendContract.Event, FriendContract.Effect>() {
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
                    with(uiState.value) { setState(copy(isRequestDialogOpened = false)) }
                }

                is FriendContract.Event.SendFriendRequest -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            friendRepository
                                .sendFriendRequest(newEvent.nickname)
                                .onSuccess {
                                    setState(copy(isRequestDialogOpened = false, isSentListChanged = true))
                                }.onFailure { exception ->
                                    // TODO: 오류 표현
                                    Log.e("Friend", "sendFriendRequest Failure", exception)
                                    setEvent(FriendContract.Event.OnError("친구 요청을 보내지 못했습니다."))
                                }
                        }
                    }
                }

                is FriendContract.Event.ResponseFriendRequest -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            friendRepository
                                .responseFriendRequest(newEvent.nickname, newEvent.isAccepted)
                                .onSuccess {
                                    val newReceivedRequests = receivedRequests.toMutableList()

                                    newReceivedRequests[newEvent.itemIndex] =
                                        FriendRequestItem(newEvent.nickname, true)

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

                is FriendContract.Event.CancelFriendRequest -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            friendRepository
                                .cancelFriendRequest(newEvent.nickname)
                                .onSuccess {
                                    val newSentRequests = sentRequests.toMutableList()

                                    newSentRequests[newEvent.itemIndex] =
                                        FriendRequestItem(newEvent.nickname, true)

                                    setState(copy(sentRequests = newSentRequests, isSentListChanged = true))
                                }.onFailure { exception ->
                                    // TODO: 오류 표현
                                    Log.e("Friend", "cancelFriendRequest Failure", exception)
                                    setEvent(FriendContract.Event.OnError("친구 요청을 취소하지 못했습니다."))
                                }
                        }
                    }
                }

                is FriendContract.Event.GetFriendList -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            if (isFriendListChanged.not()) return@launch
                            friendRepository
                                .getFriendList()
                                .onSuccess { friends ->
                                    setState(copy(friendList = friends, isFriendListChanged = false))
                                }.onFailure { exception ->
                                    // TODO: 오류 표현
                                    Log.e("Friend", "getFriendList Failure", exception)
                                    setEvent(FriendContract.Event.OnError("친구 목록을 불러오지 못했습니다."))
                                }
                        }
                    }
                }

                is FriendContract.Event.GetReceivedRequests -> {
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

                is FriendContract.Event.GetSentRequests -> {
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

                is FriendContract.Event.OnBack -> {
                    setEffect { FriendContract.Effect.PopBackStack }
                }

                is FriendContract.Event.OnError -> {
                    setEffect { FriendContract.Effect.ShowSnackbar(newEvent.errorMessage) }
                }
            }
        }
    }
