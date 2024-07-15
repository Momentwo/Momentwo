package cord.eoeo.momentwo.ui.createalbum

import cord.eoeo.momentwo.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateAlbumViewModel @Inject constructor(

) : BaseViewModel<CreateAlbumContract.State, CreateAlbumContract.Event, CreateAlbumContract.Effect>() {
    override fun createInitialState(): CreateAlbumContract.State = CreateAlbumContract.State()

    override fun handleEvent(newEvent: CreateAlbumContract.Event) {
        when (newEvent) {
            is CreateAlbumContract.Event.OnClickInviteFriend -> {
                setState(uiState.value.copy(isInviteFriendOpened = true))
            }

            is CreateAlbumContract.Event.OnDismissInviteFriend -> {
                setState(uiState.value.copy(isInviteFriendOpened = false))
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
