package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.*
internal object LocalLibraryReducer: Reducer<LocalLibraryState,LocalLibraryAction> {
    override fun reduce(state: LocalLibraryState,action: LocalLibraryAction)=when(action) {
        is LocalLibraryAction.Loaded -> state.copy(screen=ScreenState.Success(action.albums))
        LocalLibraryAction.Failed -> state.copy(screen=state.screen.failRefresh(UiError.Unknown))
    }
}
