package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.*
internal object LocalLibraryReducer : Reducer<LocalLibraryState, LocalLibraryAction> {
    override fun reduce(state: LocalLibraryState, action: LocalLibraryAction) = when (action) {
        is LocalLibraryAction.Loaded -> state.copy(screen = ScreenState.Success(action.albums))
        LocalLibraryAction.Failed -> state.copy(screen = state.screen.failRefresh(UiError.Unknown))
        is LocalLibraryAction.Open -> state.copy(creating = true, draft = action.draft, interaction = state.interaction + 1)
        LocalLibraryAction.Dismiss -> state.copy(creating = false, interaction = state.interaction + 1)
        is LocalLibraryAction.Edited -> state.copy(draft = action.draft, interaction = state.interaction + 1)
        is LocalLibraryAction.Restored -> if (state.interaction == 0L) state.copy(draft = action.draft, creating = action.draft.text.isNotEmpty()) else state
        is LocalLibraryAction.Created -> if (state.draft.token == action.token) state.copy(creating = false) else state
        is LocalLibraryAction.Pending -> state.copy(pending = action.value)
    }
}
