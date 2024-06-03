package cord.eoeo.momentwo.ui.album

import cord.eoeo.momentwo.ui.BaseViewModel

class AlbumViewModel constructor(

) : BaseViewModel<AlbumContract.State, AlbumContract.Event, AlbumContract.Effect>() {
    override fun createInitialState(): AlbumContract.State =
        AlbumContract.State(
            AlbumContract.InitState.Init
        )

    override fun handleEvent(newEvent: AlbumContract.Event) {
        when (newEvent) {
            is AlbumContract.Event.InitEvent -> {}
            is AlbumContract.Event.SecondEvent -> {
                newEvent.element
            }
        }
    }

}
