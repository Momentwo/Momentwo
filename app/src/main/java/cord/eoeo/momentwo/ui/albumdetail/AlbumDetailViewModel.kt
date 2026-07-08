package cord.eoeo.momentwo.ui.albumdetail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.data.member.MemberRepository
import cord.eoeo.momentwo.domain.album.AlbumRepository
import cord.eoeo.momentwo.domain.friend.GetFriendListUseCase
import cord.eoeo.momentwo.domain.subalbum.SubAlbumRepository
import cord.eoeo.momentwo.ui.BaseViewModel
import cord.eoeo.momentwo.ui.MomentwoDestination
import cord.eoeo.momentwo.ui.model.AlbumItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel
    @Inject
    constructor(
        private val getFriendListUseCase: GetFriendListUseCase,
        private val subAlbumRepository: SubAlbumRepository,
        private val memberRepository: MemberRepository,
        private val albumRepository: AlbumRepository,
        savedStateHandle: SavedStateHandle,
    ) : BaseViewModel<AlbumDetailContract.State, AlbumDetailContract.Event, AlbumDetailContract.Effect>() {
        init {
            val albumDetailItem = savedStateHandle.toRoute<MomentwoDestination.AlbumDetail>()
            setState(
                uiState.value.copy(
                    albumItem = AlbumItem(
                        id = albumDetailItem.id,
                        title = albumDetailItem.title,
                        subTitle = albumDetailItem.subTitle,
                        imageUrl = albumDetailItem.imageUrl,
                        subAlbumCount = 0, // TODO
                    ),
                    imageUri = Uri.parse(albumDetailItem.imageUrl),
                ),
            )
        }

        override fun createInitialState(): AlbumDetailContract.State = AlbumDetailContract.State()

        override fun handleEvent(newEvent: AlbumDetailContract.Event) {
            when (newEvent) {
                is AlbumDetailContract.Event.OnChangeNavIndex -> {
                    with(uiState.value) { setState(copy(selectedNavIndex = newEvent.index, isEditMode = false)) }
                }

                is AlbumDetailContract.Event.OnGetFriendList -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            getFriendListUseCase()
                                .onSuccess { friends ->
                                    setState(copy(friendList = friends))
                                }.onFailure { exception ->
                                    Log.e("AlbumDetail", "getFriendList onFailure", exception)
                                }
                        }
                    }
                }

                is AlbumDetailContract.Event.OnGetPermission -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            albumRepository
                                .getAlbumRole(albumItem.id)
                                .onSuccess { memberAuth ->
                                    setState(copy(permission = memberAuth))
                                }.onFailure { exception ->
                                    Log.e("AlbumDetail", "getAlbumRole onFailure", exception)
                                }
                        }
                    }
                }

                is AlbumDetailContract.Event.OnChangeFriendSelected -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            val newSelectedList = selectedFriendList.toMutableList()

                            if (newEvent.isSelected) {
                                newSelectedList.add(newEvent.friendItem)
                                setState(copy(selectedFriendList = newSelectedList))
                            } else {
                                newSelectedList.remove(newEvent.friendItem)
                                setState(copy(selectedFriendList = newSelectedList))
                            }
                        }
                    }
                }

                is AlbumDetailContract.Event.OnChangeSubAlbumSelected -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            val newSelectedList = selectedSubAlbumIds.toMutableList()

                            if (newEvent.isSelected) {
                                newSelectedList.add(newEvent.subAlbumId)
                                setState(copy(selectedSubAlbumIds = newSelectedList))
                            } else {
                                newSelectedList.remove(newEvent.subAlbumId)
                                setState(copy(selectedSubAlbumIds = newSelectedList))
                            }
                        }
                    }
                }

                is AlbumDetailContract.Event.OnChangeMemberSelected -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            val newSelectedList = selectedMemberIds.toMutableList()

                            if (newEvent.isSelected) {
                                newSelectedList.add(newEvent.memberId)
                                setState(copy(selectedMemberIds = newSelectedList))
                            } else {
                                newSelectedList.remove(newEvent.memberId)
                                setState(copy(selectedMemberIds = newSelectedList))
                            }
                        }
                    }
                }

                is AlbumDetailContract.Event.OnClickAdd -> {
                    with(uiState.value) {
                        if (selectedNavIndex == 0) {
                            setEvent(AlbumDetailContract.Event.OnOpenTextFieldDialog(0))
                        } else if (selectedNavIndex == 1) {
                            setEvent(AlbumDetailContract.Event.OnClickInvite)
                        }
                    }
                }

                is AlbumDetailContract.Event.OnClickEdit -> {
                    with(uiState.value) { setState(copy(isEditMode = true)) }
                }

                is AlbumDetailContract.Event.OnCancelEdit -> {
                    with(uiState.value) {
                        if (selectedNavIndex == 0) {
                            setState(copy(isEditMode = false, selectedSubAlbumIds = emptyList()))
                        } else if (selectedNavIndex == 1) {
                            setState(copy(isEditMode = false, selectedMemberIds = emptyList()))
                        }
                    }
                }

                is AlbumDetailContract.Event.OnConfirmEdit -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            if (selectedNavIndex == 0) {
                                setEvent(
                                    AlbumDetailContract.Event.OnSubAlbumEvents(
                                        AlbumDetailContract.SubAlbumEvents.DeleteSubAlbums(selectedSubAlbumIds),
                                    ),
                                )
                            } else if (selectedNavIndex == 1) {
                                // TODO : 멤버 권한 수정 기능 구현, Long Click 옵션으로 추가?
                                setEvent(
                                    AlbumDetailContract.Event.OnMemberEvents(
                                        AlbumDetailContract.MemberEvents.KickMembers(selectedMemberIds),
                                    ),
                                )
                            }
                        }
                    }
                }

                is AlbumDetailContract.Event.OnOpenTextFieldDialog -> {
                    with(uiState.value) { setState(copy(textFieldDialogIndex = newEvent.index)) }
                }

                is AlbumDetailContract.Event.OnDismissTextFieldDialog -> {
                    with(uiState.value) { setState(copy(textFieldDialogIndex = -1)) }
                }

                is AlbumDetailContract.Event.OnClickInvite -> {
                    with(uiState.value) { setState(copy(isInviteDialogOpened = true)) }
                }

                is AlbumDetailContract.Event.OnDismissInviteFriend -> {
                    with(uiState.value) { setState(copy(isInviteDialogOpened = false, selectedFriendList = emptyList())) }
                }

                is AlbumDetailContract.Event.OnConfirmInviteFriend -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            memberRepository
                                .requestInviteMembers(albumItem.id, selectedFriendList.map { it.nickname })
                                .onSuccess {
                                    setEvent(AlbumDetailContract.Event.OnMemberEvents(AlbumDetailContract.MemberEvents.GetMembers))
                                    setState(copy(isInviteDialogOpened = false, selectedFriendList = emptyList()))
                                }.onFailure { exception ->
                                    Log.e("AlbumDetail", "requestInviteMembers onFailure", exception)
                                }
                        }
                    }
                }

                is AlbumDetailContract.Event.OnChangeIsInChangeImage -> {
                    with(uiState.value) { setState(copy(isInChangeImage = newEvent.isInChangeImage)) }
                }

                is AlbumDetailContract.Event.OnSelectImage -> {
                    with(uiState.value) { setState(copy(imageUri = newEvent.imageUri)) }
                }

                is AlbumDetailContract.Event.OnCancelChangeImage -> {
                    with(uiState.value) {
                        setState(
                            copy(
                                isInChangeImage = false,
                                imageUri = Uri.parse(albumItem.imageUrl),
                            ),
                        )
                    }
                }

                is AlbumDetailContract.Event.OnSubAlbumEvents -> {
                    viewModelScope.launch {
                        val albumId = uiState.value.albumItem.id

                        with(newEvent.subAlbumEvents) {
                            when (this) {
                                is AlbumDetailContract.SubAlbumEvents.Create -> {
                                    subAlbumRepository
                                        .requestCreateSubAlbum(albumId, subAlbumTitle)
                                        .onSuccess {
                                            setEvent(
                                                AlbumDetailContract.Event.OnSubAlbumEvents(AlbumDetailContract.SubAlbumEvents.GetSubAlbums),
                                            )
                                            setState(uiState.value.copy(textFieldDialogIndex = -1))
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "requestCreateSubAlbum onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.SubAlbumEvents.DeleteSubAlbums -> {
                                    subAlbumRepository
                                        .deleteSubAlbums(albumId, subAlbumIds)
                                        .onSuccess {
                                            setEvent(
                                                AlbumDetailContract.Event.OnSubAlbumEvents(AlbumDetailContract.SubAlbumEvents.GetSubAlbums),
                                            )
                                            setState(uiState.value.copy(isEditMode = false))
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "deleteSubAlbums onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.SubAlbumEvents.EditTitle -> {
                                    subAlbumRepository
                                        .changeSubAlbumTitle(albumId, subAlbumId, title)
                                        .onSuccess { subAlbums ->
                                            setEvent(
                                                AlbumDetailContract.Event.OnSubAlbumEvents(AlbumDetailContract.SubAlbumEvents.GetSubAlbums),
                                            )
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "changeSubAlbumTitle onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.SubAlbumEvents.GetSubAlbums -> {
                                    subAlbumRepository
                                        .getSubAlbumList(albumId)
                                        .onSuccess { subAlbums ->
                                            setState(uiState.value.copy(subAlbumList = subAlbums))
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "getSubAlbumList onFailure", exception)
                                        }
                                }
                            }
                        }
                    }
                }

                is AlbumDetailContract.Event.OnMemberEvents -> {
                    viewModelScope.launch {
                        val albumId = uiState.value.albumItem.id

                        with(newEvent.memberEvents) {
                            when (this) {
                                is AlbumDetailContract.MemberEvents.AssignAdmin -> {
                                    memberRepository
                                        .assignAdminToMember(albumId, nickname)
                                        .onSuccess {
                                            setEvent(AlbumDetailContract.Event.OnMemberEvents(AlbumDetailContract.MemberEvents.GetMembers))
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "assignAdminToMember onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.MemberEvents.EditPermissions -> {
                                    memberRepository
                                        .editMembersPermission(albumId, editMembers)
                                        .onSuccess {
                                            setEvent(AlbumDetailContract.Event.OnMemberEvents(AlbumDetailContract.MemberEvents.GetMembers))
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "editMembersPermission onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.MemberEvents.Exit -> {
                                    memberRepository
                                        .exitFromAlbum(albumId)
                                        .onSuccess {
                                            setEvent(AlbumDetailContract.Event.OnBack)
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "exitFromAlbum onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.MemberEvents.GetMembers -> {
                                    memberRepository
                                        .getMemberList(albumId)
                                        .onSuccess { members ->
                                            setState(uiState.value.copy(memberList = members))
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "getMemberList onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.MemberEvents.KickMembers -> {
                                    memberRepository
                                        .kickMembers(albumId, kickMemberIds)
                                        .onSuccess {
                                            setEvent(AlbumDetailContract.Event.OnMemberEvents(AlbumDetailContract.MemberEvents.GetMembers))
                                            setState(uiState.value.copy(isEditMode = false))
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "kickMembers onFailure", exception)
                                        }
                                }
                            }
                        }
                    }
                }

                is AlbumDetailContract.Event.OnAlbumSettingEvents -> {
                    viewModelScope.launch {
                        val albumId = uiState.value.albumItem.id

                        with(newEvent.albumSettingEvents) {
                            when (this) {
                                is AlbumDetailContract.AlbumSettingEvents.ChangeImage -> {
                                    uiState.value.imageUri?.let { imageUri ->
                                        albumRepository
                                            .changeAlbumImage(albumId, imageUri)
                                            .onSuccess {
                                                setEffect { AlbumDetailContract.Effect.PopBackStackInAlbumDetail }
                                            }.onFailure { exception ->
                                                Log.e("AlbumDetail", "changeAlbumImage onFailure", exception)
                                            }
                                    } ?: albumRepository
                                        .deleteAlbumImage(albumId)
                                        .onFailure { exception ->
                                            Log.e("AlbumDetail", "deleteAlbumImage onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.AlbumSettingEvents.DeleteAlbum -> {
                                    albumRepository
                                        .deleteAlbum(albumId)
                                        .onSuccess {
                                            setEvent(AlbumDetailContract.Event.OnBack)
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "deleteAlbum onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.AlbumSettingEvents.DeleteSubTitle -> {
                                    albumRepository
                                        .deleteAlbumSubTitle(albumId)
                                        .onSuccess {
                                            setState(
                                                uiState.value.copy(
                                                    albumItem = uiState.value.albumItem.copy(subTitle = ""),
                                                    textFieldDialogIndex = -1,
                                                ),
                                            )
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "deleteAlbumSubTitle onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.AlbumSettingEvents.EditTitle -> {
                                    albumRepository
                                        .changeAlbumTitle(albumId, title)
                                        .onSuccess {
                                            setState(
                                                uiState.value.copy(
                                                    albumItem = uiState.value.albumItem.copy(title = title),
                                                    textFieldDialogIndex = -1,
                                                ),
                                            )
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "changeAlbumTitle onFailure", exception)
                                        }
                                }

                                is AlbumDetailContract.AlbumSettingEvents.EditSubTitle -> {
                                    albumRepository
                                        .changeAlbumSubTitle(albumId, subTitle)
                                        .onSuccess {
                                            setState(
                                                uiState.value.copy(
                                                    albumItem = uiState.value.albumItem.copy(subTitle = subTitle),
                                                    textFieldDialogIndex = -1,
                                                ),
                                            )
                                        }.onFailure { exception ->
                                            Log.e("AlbumDetail", "changeAlbumSubTitle onFailure", exception)
                                        }
                                }
                            }
                        }
                    }
                }

                is AlbumDetailContract.Event.OnBack -> {
                    setEffect { AlbumDetailContract.Effect.PopBackStack }
                }

                is AlbumDetailContract.Event.OnBackInAlbumDetail -> {
                    setEffect { AlbumDetailContract.Effect.PopBackStackInAlbumDetail }
                }

                is AlbumDetailContract.Event.OnError -> {
                    setEffect { AlbumDetailContract.Effect.ShowSnackbar(newEvent.errorMessage) }
                }
            }
        }
    }
