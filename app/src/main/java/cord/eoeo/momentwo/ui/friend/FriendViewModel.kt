package cord.eoeo.momentwo.ui.friend

import cord.eoeo.momentwo.ui.BaseViewModel
import cord.eoeo.momentwo.ui.model.UserItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FriendViewModel
    @Inject
    constructor() : BaseViewModel<FriendContract.State, FriendContract.Event, FriendContract.Effect>() {
        override fun createInitialState(): FriendContract.State = FriendContract.State()

        override fun handleEvent(newEvent: FriendContract.Event) {
            when (newEvent) {
                is FriendContract.Event.OnChangeNavIndex -> {
                    with(uiState.value) { setState(copy(selectedNavIndex = newEvent.index)) }
                }

                is FriendContract.Event.getFriendList -> {
                    // TODO: Get Friend List
                    val newFriendList =
                        listOf(
                            UserItem(id = 1, nickname = "User1"),
                            UserItem(id = 2, nickname = "User2"),
                            UserItem(id = 3, nickname = "User3"),
                            UserItem(id = 4, nickname = "User4"),
                            UserItem(id = 5, nickname = "User5"),
                            UserItem(id = 6, nickname = "User6"),
                        )

                    with(uiState.value) {
                        if (isFriendListChanged) {
                            setState(copy(friendList = newFriendList, isFriendListChanged = false))
                        }
                    }
                }

                is FriendContract.Event.getReceivedRequests -> {
                    // TODO: Get Received Request List
                    val newReceivedRequests =
                        listOf(
                            UserItem(id = 7, nickname = "User7"),
                            UserItem(id = 8, nickname = "User8"),
                            UserItem(id = 9, nickname = "User9"),
                            UserItem(id = 10, nickname = "User10"),
                        )

                    with(uiState.value) {
                        if (isReceivedListChanged) {
                            setState(copy(receivedRequests = newReceivedRequests, isReceivedListChanged = false))
                        }
                    }
                }

                is FriendContract.Event.getSentRequests -> {
                    // TODO: Get Sent Request List
                    val newSentRequests =
                        listOf(
                            UserItem(id = 11, nickname = "User11"),
                            UserItem(id = 12, nickname = "User12"),
                            UserItem(id = 13, nickname = "User13"),
                            UserItem(id = 14, nickname = "User14"),
                            UserItem(id = 15, nickname = "User15"),
                        )

                    with(uiState.value) {
                        if (isSentListChanged) {
                            setState(copy(sentRequests = newSentRequests, isSentListChanged = false))
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
