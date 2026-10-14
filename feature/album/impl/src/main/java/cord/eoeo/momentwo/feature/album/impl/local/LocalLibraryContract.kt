package cord.eoeo.momentwo.feature.album.impl.local

import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.LocalAlbum

internal data class LocalLibraryState(val screen: ScreenState<List<LocalAlbum>> = ScreenState.Loading) : UiState
internal sealed interface LocalLibraryEvent : UiEvent { data object Retry : LocalLibraryEvent; data object ConnectAccount : LocalLibraryEvent }
internal sealed interface LocalLibraryAction { data class Loaded(val albums: List<LocalAlbum>) : LocalLibraryAction; data object Failed : LocalLibraryAction }
internal sealed interface LocalLibraryEffect : UiEffect { data object Login : LocalLibraryEffect }
internal object LocalLibraryReducer : Reducer<LocalLibraryState, LocalLibraryAction> {
    override fun reduce(state: LocalLibraryState, action: LocalLibraryAction) = when (action) {
        is LocalLibraryAction.Loaded -> state.copy(screen = ScreenState.Success(action.albums))
        LocalLibraryAction.Failed -> state.copy(screen = ScreenState.Error(UiError.Unknown))
    }
}
