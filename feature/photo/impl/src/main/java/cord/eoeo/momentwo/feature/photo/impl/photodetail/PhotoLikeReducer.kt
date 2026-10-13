package cord.eoeo.momentwo.feature.photo.impl.photodetail

import cord.eoeo.momentwo.core.common.*

internal object PhotoLikeReducer : Reducer<PhotoDetailState, PhotoLikeAction> {
    override fun reduce(state: PhotoDetailState, action: PhotoLikeAction): PhotoDetailState {
        val current = state.screen as? ScreenState.Success ?: return state
        return when (action) {
            is PhotoLikeAction.Optimistic -> state.copy(screen = current.copy(content = current.content.copy(isLiked = action.liked, likeCount = action.count)),
                likeLastUpdated = action.timestamp, likeVersion = state.likeVersion + 1)
            is PhotoLikeAction.Rollback -> if (state.likeVersion == action.version)
                state.copy(screen = current.copy(content = current.content.copy(isLiked = action.liked, likeCount = action.count))) else state
        }
    }
}
