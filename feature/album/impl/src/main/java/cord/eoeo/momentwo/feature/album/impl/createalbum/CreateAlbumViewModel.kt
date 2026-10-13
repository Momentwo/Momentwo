package cord.eoeo.momentwo.feature.album.impl.createalbum

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.domain.album.RequestCreateAlbumUseCase
import cord.eoeo.momentwo.core.domain.friend.GetFriendListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class CreateAlbumViewModel @Inject constructor(
    private val requestCreateAlbumUseCase: RequestCreateAlbumUseCase,
    private val getFriendListUseCase: GetFriendListUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ReducerViewModel<CreateAlbumState, CreateAlbumEvent, CreateAlbumAction, CreateAlbumEffect>(
    CreateAlbumState(title = savedStateHandle["create_album_title"] ?: ""), CreateAlbumReducer,
) {
    private data object Friends : RequestKey
    private data object Submit : RequestKey
    override fun handleEvent(event: CreateAlbumEvent) {
        when (event) {
            is CreateAlbumEvent.OnTitleChanged -> { savedStateHandle["create_album_title"] = event.title; dispatch(CreateAlbumAction.TitleChanged(event.title)) }
            CreateAlbumEvent.OnClickInviteFriend -> { dispatch(CreateAlbumAction.OpenInvite); loadFriends() }
            is CreateAlbumEvent.OnClearFriendItem -> dispatch(CreateAlbumAction.Removed(event.nickname))
            is CreateAlbumEvent.OnCheckedChange -> dispatch(CreateAlbumAction.Checked(event.isChecked, event.friendItem))
            CreateAlbumEvent.OnDismissInviteFriend -> dispatch(CreateAlbumAction.DismissInvite)
            CreateAlbumEvent.OnConfirmInviteFriend -> dispatch(CreateAlbumAction.ConfirmInvite)
            CreateAlbumEvent.OnGetFriendList -> loadFriends(force = true)
            is CreateAlbumEvent.OnRequestCreateAlbum -> submit(event.title)
            CreateAlbumEvent.OnBack -> sendEffect(CreateAlbumEffect.PopBackStack)
            is CreateAlbumEvent.OnError -> sendEffect(CreateAlbumEffect.ShowSnackbar(event.errorMessage.ifBlank { "앨범을 만들지 못했습니다" }))
        }
    }
    private fun loadFriends(force: Boolean = false) {
        if (!force && uiState.value.friends is ScreenState.Success) return
        requests.launchRequest(Friends, RequestPolicy.LatestWins, request = { getFriendListUseCase() },
            onStarted = { dispatch(CreateAlbumAction.FriendsStarted) }, onSuccess = { dispatch(CreateAlbumAction.FriendsLoaded(it)) },
            onFailure = { Log.e("CreateAlbum", "Friend list failed", it); dispatch(CreateAlbumAction.FriendsFailed(it.toUiError())) },
            onFinished = { dispatch(CreateAlbumAction.FriendsFinished) })
    }
    private fun submit(title: String) {
        val names = uiState.value.selectedFriendList.map { it.nickname }
        val revision = uiState.value.revision
        requests.launchRequest(Submit, RequestPolicy.IgnoreWhileRunning, request = { requestCreateAlbumUseCase(title, names) },
            onStarted = { dispatch(CreateAlbumAction.Started) },
            onSuccess = {
                dispatch(CreateAlbumAction.Succeeded)
                if (uiState.value.revision == revision) emitEffect(CreateAlbumEffect.PopBackStack)
                else emitEffect(CreateAlbumEffect.ShowSnackbar("요청한 앨범을 만들었습니다"))
            },
            onFailure = { Log.e("CreateAlbum", "Creation failed", it); dispatch(CreateAlbumAction.Failed(it.toUiError())); emitEffect(CreateAlbumEffect.ShowSnackbar("앨범을 만들지 못했습니다")) },
            onFinished = { dispatch(CreateAlbumAction.Finished) })
    }
}
