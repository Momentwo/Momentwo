package cord.eoeo.momentwo.feature.photo.impl.photodetail

import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.DescriptionItem

internal object PhotoDescriptionReducer : Reducer<PhotoDetailState, PhotoDescriptionAction> {
    override fun reduce(state: PhotoDetailState, action: PhotoDescriptionAction): PhotoDetailState = when (action) {
        is PhotoDescriptionAction.Editing -> state.copy(isWriteDescMode = action.enabled, descriptionRevision = state.descriptionRevision + 1, descriptionSession = state.descriptionSession + 1)
        PhotoDescriptionAction.DraftEdited -> state.copy(descriptionRevision = state.descriptionRevision + 1)
        is PhotoDescriptionAction.Committed -> state.copy(descriptionVersion = state.descriptionVersion + 1, confirmedDescriptionRevision = action.revision)
        is PhotoDescriptionAction.Deleted -> {
            val screen = when (val current = state.screen) {
                ScreenState.Loading -> current
                is ScreenState.Error -> current
                is ScreenState.Success -> current.copy(content = current.content.copy(description = DescriptionItem("", "", "", "", emptyList())))
            }
            state.copy(screen = screen, descriptionVersion = state.descriptionVersion + 1,
                isWriteDescMode = if (state.descriptionRevision == action.revision) false else state.isWriteDescMode)
        }
    }
}
