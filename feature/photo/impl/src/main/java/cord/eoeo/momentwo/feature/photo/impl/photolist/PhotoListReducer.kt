package cord.eoeo.momentwo.feature.photo.impl.photolist

import cord.eoeo.momentwo.core.common.*

internal object PhotoListReducer : Reducer<PhotoListState, PhotoListAction> {
    override fun reduce(state: PhotoListState, action: PhotoListAction): PhotoListState = when (action) {
        PhotoListAction.SourceStarted -> state.copy(source = ScreenState.Loading)
        is PhotoListAction.SourceReady -> state.copy(source = ScreenState.Success(action.source))
        is PhotoListAction.SourceFailed -> state.copy(source = ScreenState.Error(action.error))
        is PhotoListAction.MenuChanged -> state.copy(isMenuExpended = action.expanded)
        is PhotoListAction.EditChanged -> state.copy(isEditMode = action.enabled, editSession = state.editSession + 1)
        PhotoListAction.CancelEdit -> state.copy(isEditMode = false, selectedPhotoIds = emptyList(), editSession = state.editSession + 1)
        is PhotoListAction.SelectionChanged -> state.copy(selectedPhotoIds = if (action.selected) (state.selectedPhotoIds + action.id).distinct() else state.selectedPhotoIds.filterNot { it == action.id })
        is PhotoListAction.DialogChanged -> state.copy(isDialogOpened = action.open, dialogRevision = state.dialogRevision + 1, dialogSession = state.dialogSession + 1)
        PhotoListAction.DialogEdited -> state.copy(dialogRevision = state.dialogRevision + 1)
        is PhotoListAction.Renamed -> state.copy(subAlbumTitle = action.title, isDialogOpened = if (state.dialogRevision == action.revision) false else state.isDialogOpened)
        is PhotoListAction.Deleted -> {
            val remaining = state.selectedPhotoIds - action.ids.toSet()
            state.copy(selectedPhotoIds = remaining, isEditMode = if (state.editSession == action.session && remaining.isEmpty()) false else state.isEditMode)
        }
        is PhotoListAction.PendingChanged -> state.copy(pendingWrites = if (action.pending) state.pendingWrites + action.key else state.pendingWrites - action.key)
        is PhotoListAction.DirtyChanged -> state.copy(dirty = action.dirty)
    }
}
