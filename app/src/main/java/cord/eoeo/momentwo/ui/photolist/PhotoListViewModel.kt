package cord.eoeo.momentwo.ui.photolist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.data.photo.PhotoRepository
import cord.eoeo.momentwo.ui.BaseViewModel
import cord.eoeo.momentwo.ui.MomentwoDestination
import cord.eoeo.momentwo.ui.photolist.PhotoListContract.Effect.ShowSnackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoListViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PhotoListContract.State, PhotoListContract.Event, PhotoListContract.Effect>() {
    init {
        val (albumId, subAlbumId, albumTitle, subAlbumTitle) = savedStateHandle.toRoute<MomentwoDestination.PhotoList>()

        viewModelScope.launch {
            setState(
                uiState.value.copy(
                    albumId = albumId,
                    subAlbumId = subAlbumId,
                    albumTitle = albumTitle,
                    subAlbumTitle = subAlbumTitle,
                    photoPagingData = photoRepository.getPhotoPagingData(50, albumId, subAlbumId),
                )
            )
        }
    }

    override fun createInitialState(): PhotoListContract.State = PhotoListContract.State()

    override fun handleEvent(newEvent: PhotoListContract.Event) {
        when (newEvent) {
            is PhotoListContract.Event.OnChangeIsMenuExpended -> {
                with(uiState.value) { setState(copy(isMenuExpended = newEvent.isMenuExpended)) }
            }

            is PhotoListContract.Event.OnChangeIsEditMode -> {
                with(uiState.value) { setState(copy(isEditMode = newEvent.isEditMode)) }
            }

            is PhotoListContract.Event.OnChangeIsDialogOpened -> {
                with(uiState.value) { setState(copy(isDialogOpened = newEvent.isDialogOpened)) }
            }

            is PhotoListContract.Event.OnConfirmDialog -> {
                with(uiState.value) { setState(copy(subAlbumTitle = newEvent.subAlbumTitle, isDialogOpened = false)) }
            }

            is PhotoListContract.Event.OnBack -> {
                setEffect { PhotoListContract.Effect.PopBackStack }
            }

            is PhotoListContract.Event.OnError -> {
                setEffect { ShowSnackbar(newEvent.errorMessage) }
            }
        }
    }
}
