package cord.eoeo.momentwo.feature.photo.impl.photodetail

import cord.eoeo.momentwo.core.common.Reducer

internal object PhotoUiReducer : Reducer<PhotoDetailState, PhotoUiAction> {
    override fun reduce(state: PhotoDetailState, action: PhotoUiAction): PhotoDetailState = when (action) {
        PhotoUiAction.ToggleMenu -> state.copy(isMenuVisible = !state.isMenuVisible)
        is PhotoUiAction.DescriptionVisible -> state.copy(isDescriptionVisible = action.visible,
            isWriteDescMode = if (action.visible) state.isWriteDescMode else false,
            descriptionRevision = state.descriptionRevision + 1)
        is PhotoUiAction.CommentVisible -> state.copy(isCommentVisible = action.visible)
    }
}
