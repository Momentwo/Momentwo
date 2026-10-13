package cord.eoeo.momentwo.feature.photo.impl.photodetail

import cord.eoeo.momentwo.core.common.*

internal object PhotoDetailReducer : Reducer<PhotoDetailState, PhotoDetailAction> {
    override fun reduce(state: PhotoDetailState, action: PhotoDetailAction): PhotoDetailState = when (action) {
        is PhotoUiAction -> PhotoUiReducer.reduce(state, action)
        is PhotoLikeAction -> PhotoLikeReducer.reduce(state, action)
        is PhotoDescriptionAction -> PhotoDescriptionReducer.reduce(state, action)
        is PhotoCommentAction -> PhotoCommentReducer.reduce(state, action)
        PhotoDetailAction.Started -> state.copy(screen = state.screen.startRefresh())
        is PhotoDetailAction.Loaded -> {
            val existing = (state.screen as? ScreenState.Success)?.content
            val content = if (existing == null) PhotoDetailContent(action.description, action.count, state.initialIsLiked)
                else existing.copy(
                    description = if (state.descriptionVersion == action.descriptionVersion) action.description else existing.description,
                    likeCount = if (state.likeVersion == action.likeVersion) action.count else existing.likeCount,
                )
            val confirmed = state.confirmedDescriptionRevision
            val currentDescription = state.descriptionVersion == action.descriptionVersion
            state.copy(screen = ScreenState.Success(content),
                isWriteDescMode = if (currentDescription && confirmed != null && confirmed == state.descriptionRevision) false else state.isWriteDescMode,
                confirmedDescriptionRevision = if (currentDescription) null else confirmed)
        }
        is PhotoDetailAction.Failed -> state.copy(screen = state.screen.failRefresh(action.error))
        PhotoDetailAction.Finished -> state.copy(screen = state.screen.cancelRefresh())
        is PhotoDetailAction.PendingChanged -> state.copy(pendingWrites = if (action.pending) state.pendingWrites + action.key else state.pendingWrites - action.key)
    }
}
