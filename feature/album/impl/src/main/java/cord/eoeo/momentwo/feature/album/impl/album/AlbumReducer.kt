package cord.eoeo.momentwo.feature.album.impl.album

import cord.eoeo.momentwo.core.common.*

internal object AlbumReducer : Reducer<AlbumState, AlbumAction> {
    override fun reduce(state: AlbumState, action: AlbumAction): AlbumState = when (action) {
        AlbumAction.Started -> state.copy(screen = state.screen.startRefresh())
        is AlbumAction.Loaded -> if (state.revision == action.revision) state.copy(screen = ScreenState.Success(action.content), dirty = false) else state
        is AlbumAction.Failed -> state.copy(screen = state.screen.failRefresh(action.error), dirty = false)
        AlbumAction.Finished -> state.copy(screen = state.screen.cancelRefresh())
        AlbumAction.Invalidated -> state.copy(dirty = true, revision = state.revision + 1)
    }
}
