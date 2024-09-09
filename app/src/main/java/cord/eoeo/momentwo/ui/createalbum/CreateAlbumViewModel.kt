package cord.eoeo.momentwo.ui.createalbum

import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.domain.album.RequestCreateAlbumUseCase
import cord.eoeo.momentwo.domain.friend.GetFriendListUseCase
import cord.eoeo.momentwo.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAlbumViewModel
    @Inject
    constructor(
        private val requestCreateAlbumUseCase: RequestCreateAlbumUseCase,
        private val getFriendListUseCase: GetFriendListUseCase,
    ) : BaseViewModel<CreateAlbumContract.State, CreateAlbumContract.Event, CreateAlbumContract.Effect>() {
        override fun createInitialState(): CreateAlbumContract.State = CreateAlbumContract.State()

        override fun handleEvent(newEvent: CreateAlbumContract.Event) {
            when (newEvent) {
                is CreateAlbumContract.Event.OnClickInviteFriend -> {
                    with(uiState.value) {
                        setState(
                            copy(
                                tempSelectedFriendList = selectedFriendList.toList(),
                                isInviteFriendOpened = true,
                            ),
                        )
                    }
                }

                is CreateAlbumContract.Event.OnClearFriendItem -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            val newSelectedList = selectedFriendList.toMutableList()
                            val newTempMap = tempSelectedFriendMap.toMutableMap()

                            newSelectedList.removeIf { it.nickname == newEvent.nickname }
                            newTempMap[newEvent.nickname] = false

                            setState(
                                copy(
                                    selectedFriendList = newSelectedList,
                                    tempSelectedFriendList = newSelectedList,
                                    tempSelectedFriendMap = newTempMap,
                                ),
                            )
                        }
                    }
                }

                is CreateAlbumContract.Event.OnCheckedChange -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            val newTempList = tempSelectedFriendList.toMutableList()
                            val newTempMap = tempSelectedFriendMap.toMutableMap()

                            if (newEvent.isChecked) {
                                newTempList.add(newEvent.friendItem)
                            } else {
                                newTempList.remove(newEvent.friendItem)
                            }
                            newTempMap[newEvent.friendItem.nickname] = newEvent.isChecked

                            setState(
                                copy(
                                    tempSelectedFriendList = newTempList,
                                    tempSelectedFriendMap = newTempMap,
                                ),
                            )
                        }
                    }
                }

                is CreateAlbumContract.Event.OnDismissInviteFriend -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            val newTempMap = friendList.associate { it.nickname to false }.toMutableMap()
                            selectedFriendList.forEach { newTempMap[it.nickname] = true }

                            setState(
                                copy(
                                    tempSelectedFriendList = selectedFriendList.toList(),
                                    tempSelectedFriendMap = newTempMap,
                                    isInviteFriendOpened = false,
                                ),
                            )
                        }
                    }
                }

                is CreateAlbumContract.Event.OnConfirmInviteFriend -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            setState(
                                copy(
                                    selectedFriendList = tempSelectedFriendList.toList(),
                                    isInviteFriendOpened = false,
                                ),
                            )
                        }
                    }
                }

                is CreateAlbumContract.Event.OnGetFriendList -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                        /*val newFriendList =
                            listOf(
                                FriendItem("tester1"),
                                FriendItem("tester4"),
                                FriendItem("tester3"),
                                FriendItem("tester2"),
                                FriendItem("friend1"),
                                FriendItem("friend2"),
                                FriendItem("friend3"),
                                FriendItem("friend4"),
                            )
                        setState(
                            copy(
                                friendList = newFriendList,
                                tempSelectedFriendMap = newFriendList.associate { it.nickname to false },
                            ),
                        )*/

                            getFriendListUseCase()
                                .onSuccess { newFriendList ->
                                    setState(
                                        copy(
                                            friendList = newFriendList,
                                            tempSelectedFriendMap = newFriendList.associate { it.nickname to false },
                                        ),
                                    )
                                }.onFailure { exception ->
                                    // TODO: 오류 처리
                                    Log.e("CreateAlbum", "getFriendList Failure", exception)
                                }
                        }
                    }
                }

                is CreateAlbumContract.Event.OnRequestCreateAlbum -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            requestCreateAlbumUseCase(newEvent.title, selectedFriendList.map { it.nickname })
                                .onSuccess {
                                    // TODO: 성공 or 실패 Dialog 표시?
                                    setEffect { CreateAlbumContract.Effect.PopBackStack }
                                }.onFailure { exception ->
                                    Log.e("CreateAlbum", "requestCreateAlbum Failure", exception)
                                }
                        }
                    }
                }

                is CreateAlbumContract.Event.OnBack -> {
                    setEffect { CreateAlbumContract.Effect.PopBackStack }
                }

                is CreateAlbumContract.Event.OnError -> {
                    setEffect { CreateAlbumContract.Effect.ShowSnackbar(newEvent.errorMessage) }
                }
            }
        }
    }
