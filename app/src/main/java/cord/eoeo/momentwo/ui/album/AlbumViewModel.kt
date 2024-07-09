package cord.eoeo.momentwo.ui.album

import cord.eoeo.momentwo.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(

) : BaseViewModel<AlbumContract.State, AlbumContract.Event, AlbumContract.Effect>() {
    override fun createInitialState(): AlbumContract.State = AlbumContract.State()

    override fun handleEvent(newEvent: AlbumContract.Event) {
        when (newEvent) {
            is AlbumContract.Event.OnBack -> {
                if (newEvent.isDrawerOpen) {
                    setEffect { AlbumContract.Effect.CloseDrawer }
                } else {
                    setEffect { AlbumContract.Effect.PopBackStack }
                }
            }

            is AlbumContract.Event.OnError -> {
                setEffect { AlbumContract.Effect.ShowSnackbar(newEvent.errorMessage) }
            }
        }
    }
}
