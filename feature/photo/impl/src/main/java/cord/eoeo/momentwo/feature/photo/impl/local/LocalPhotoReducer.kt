package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.*
internal object LocalPhotoReducer : Reducer<LocalPhotoState, LocalPhotoAction> {
    override fun reduce(state: LocalPhotoState, action: LocalPhotoAction) = when (action) {
        is LocalPhotoAction.Snapshot -> state.copy(screen = ScreenState.Success(action.content))
        is LocalPhotoAction.Failed -> if(action.error==UiError.NotFound) state.copy(screen=ScreenState.Error(action.error),dialog=LocalPhotoDialog.None,draft=cord.eoeo.momentwo.core.model.LibraryDraft("","",""),interaction=state.interaction+1, immersive=false, commentsOpen=false) else state.copy(screen=state.screen.failRefresh(action.error))
        is LocalPhotoAction.Open -> state.copy(dialog = action.dialog, draft = action.draft, interaction = state.interaction + 1)
        is LocalPhotoAction.Restored -> if (state.interaction == 0L) state.copy(dialog = action.dialog, draft = action.draft) else state
        LocalPhotoAction.Dismiss -> state.copy(dialog = LocalPhotoDialog.None, interaction = state.interaction + 1)
        is LocalPhotoAction.Edited -> state.copy(draft = action.draft, interaction = state.interaction + 1)
        is LocalPhotoAction.Saved -> if (state.draft.token == action.token && state.interaction == action.interaction) state.copy(dialog = LocalPhotoDialog.None) else state
        LocalPhotoAction.OpenComments->state.copy(commentsOpen=true)
        is LocalPhotoAction.CommentFailed->state.copy(screen=when(val screen=state.screen){is ScreenState.Success->screen.copy(content=screen.content.copy(comments=screen.content.comments.failRefresh(action.error)));ScreenState.Loading,is ScreenState.Error->screen})
        LocalPhotoAction.ToggleImmersive -> state.copy(immersive = !state.immersive)
        is LocalPhotoAction.Pending -> state.copy(pending = if (action.value) state.pending + action.key else state.pending - action.key)
    }
}
