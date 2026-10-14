package cord.eoeo.momentwo.feature.albumdetail.impl.local
import cord.eoeo.momentwo.core.common.*
internal object LocalAlbumReducer : Reducer<LocalAlbumState, LocalAlbumAction> {
    override fun reduce(state: LocalAlbumState, action: LocalAlbumAction) = when (action) {
        is LocalAlbumAction.Snapshot -> state.copy(screen = ScreenState.Success(action.content))
        is LocalAlbumAction.Failed -> state.copy(screen = if (action.error == UiError.NotFound) ScreenState.Error(action.error) else state.screen.failRefresh(action.error))
        is LocalAlbumAction.Open -> state.copy(dialog = action.dialog, draft = action.draft, interaction = state.interaction + 1)
        is LocalAlbumAction.Restored -> if (state.interaction == 0L) state.copy(dialog = action.dialog, draft = action.draft) else state
        LocalAlbumAction.Dismiss -> state.copy(dialog = LocalAlbumDialog.None, interaction = state.interaction + 1)
        is LocalAlbumAction.Edited -> state.copy(draft = action.draft, interaction = state.interaction + 1)
        is LocalAlbumAction.Saved -> if (state.draft.token == action.token && state.interaction == action.interaction) state.copy(dialog = LocalAlbumDialog.None) else state
        is LocalAlbumAction.Pending -> state.copy(pending = if (action.value) state.pending + action.key else state.pending - action.key)
    }
}
