package cord.eoeo.momentwo.feature.photo.impl.photodetail

import cord.eoeo.momentwo.core.common.Reducer

internal object PhotoCommentReducer : Reducer<PhotoDetailState, PhotoCommentAction> {
    override fun reduce(state: PhotoDetailState, action: PhotoCommentAction): PhotoDetailState = when (action) {
        is PhotoCommentAction.SourceReady -> state.copy(commentPagingData = action.source, commentsReady = true, commentsError = null)
        is PhotoCommentAction.SourceFailed -> state.copy(commentsError = action.error)
        is PhotoCommentAction.DraftChanged -> state.copy(commentDraft = action.text, commentRevision = state.commentRevision + 1)
        is PhotoCommentAction.DialogOpened -> state.copy(commentDialog = PhotoCommentDialog.Edit(action.id, action.text), commentDialogRevision = state.commentDialogRevision + 1, commentDialogSession = state.commentDialogSession + 1)
        PhotoCommentAction.DialogDismissed -> state.copy(commentDialog = PhotoCommentDialog.None, commentDialogRevision = state.commentDialogRevision + 1)
        PhotoCommentAction.DialogEdited -> state.copy(commentDialogRevision = state.commentDialogRevision + 1)
        is PhotoCommentAction.Edited -> if ((state.commentDialog as? PhotoCommentDialog.Edit)?.id == action.id && state.commentDialogRevision == action.revision)
            state.copy(commentDialog = PhotoCommentDialog.None) else state
        is PhotoCommentAction.Sent -> if (state.commentRevision == action.revision) state.copy(commentDraft = "") else state
    }
}
