package cord.eoeo.momentwo.ui.albumdetail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import cord.eoeo.momentwo.ui.BaseViewModel
import cord.eoeo.momentwo.ui.MomentwoDestination
import cord.eoeo.momentwo.ui.model.AlbumItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<AlbumDetailContract.State, AlbumDetailContract.Event, AlbumDetailContract.Effect>() {
    init {
        setState(
            uiState.value.copy(
                albumItem = AlbumItem.newInstance(savedStateHandle.toRoute<MomentwoDestination.AlbumDetail>())
            )
        )
    }

    override fun createInitialState(): AlbumDetailContract.State = AlbumDetailContract.State()

    override fun handleEvent(newEvent: AlbumDetailContract.Event) {
        when (newEvent) {
            is AlbumDetailContract.Event.OnChangeNavIndex -> {
                with(uiState.value) { setState(copy(selectedNavIndex = newEvent.index)) }
            }

            is AlbumDetailContract.Event.OnBack -> {
                setEffect { AlbumDetailContract.Effect.PopBackStack }
            }

            is AlbumDetailContract.Event.OnError -> {
                setEffect { AlbumDetailContract.Effect.ShowSnackbar(newEvent.errorMessage) }
            }
        }
    }
}
