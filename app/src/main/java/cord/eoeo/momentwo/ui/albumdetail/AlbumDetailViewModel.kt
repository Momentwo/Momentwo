package cord.eoeo.momentwo.ui.albumdetail

import cord.eoeo.momentwo.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel
    @Inject
    constructor() :
    BaseViewModel<AlbumDetailContract.State, AlbumDetailContract.Event, AlbumDetailContract.Effect>() {
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
