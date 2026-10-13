package cord.eoeo.momentwo.feature.albumdetail.impl

import cord.eoeo.momentwo.core.common.*

internal object SubAlbumReducer : Reducer<AlbumDetailState, SubAlbumAction> {
    override fun reduce(state: AlbumDetailState, action: SubAlbumAction): AlbumDetailState = when (action) {
        SubAlbumAction.Started -> state.copy(subAlbums = state.subAlbums.startRefresh())
        is SubAlbumAction.Loaded -> if (state.subAlbumVersion == action.version) state.copy(subAlbums = ScreenState.Success(action.content), subAlbumsDirty = false) else state
        is SubAlbumAction.Failed -> state.copy(subAlbums = state.subAlbums.failRefresh(action.error))
        SubAlbumAction.Finished -> state.copy(subAlbums = state.subAlbums.cancelRefresh())
        SubAlbumAction.Invalidated -> state.copy(subAlbumsDirty = true)
        SubAlbumAction.Committed -> state.copy(subAlbumVersion = state.subAlbumVersion + 1)
        is SubAlbumAction.Deleted -> {
            val remaining = state.selectedSubAlbumIds - action.ids.toSet()
            val content = when (val current = state.subAlbums) {
                ScreenState.Loading -> current
                is ScreenState.Error -> current
                is ScreenState.Success -> current.copy(content = current.content.copy(items = current.content.items.filterNot { it.id in action.ids }))
            }
            state.copy(subAlbums = content, selectedSubAlbumIds = remaining, subAlbumVersion = state.subAlbumVersion + 1,
                isEditMode = if (state.editRevision == action.editRevision && remaining.isEmpty()) false else state.isEditMode)
        }
    }
}
