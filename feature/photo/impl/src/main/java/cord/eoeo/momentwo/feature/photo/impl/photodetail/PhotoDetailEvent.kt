package cord.eoeo.momentwo.feature.photo.impl.photodetail

import cord.eoeo.momentwo.core.common.UiEvent

internal sealed interface PhotoDetailEvent : UiEvent {
    data class OnOpenCommentDialog(val id: Int, val text: String) : PhotoDetailEvent
    data object OnDismissCommentDialog : PhotoDetailEvent
    data object OnCommentEditDraftChanged : PhotoDetailEvent
    data object OnRetry : PhotoDetailEvent
    data object OnDescriptionDraftChanged : PhotoDetailEvent
    data class OnCommentDraftChanged(val text: String) : PhotoDetailEvent

    data object OnToggleIsMenuVisible : PhotoDetailEvent

    data object OnToggleIsLiked : PhotoDetailEvent

    data object OnGetLikeCount : PhotoDetailEvent

    data class OnChangeIsDescriptionVisible(
        val isDescriptionVisible: Boolean,
    ) : PhotoDetailEvent

    data class OnChangeIsWriteDescMode(
        val isWriteDescMode: Boolean,
    ) : PhotoDetailEvent

    data object OnGetDescription : PhotoDetailEvent

    data class OnWriteDescription(
        val title: String,
        val contents: String,
    ) : PhotoDetailEvent

    data class OnEditDescription(
        val title: String,
        val contents: String,
    ) : PhotoDetailEvent

    data object OnDeleteDescription : PhotoDetailEvent

    data class OnChangeIsCommentVisible(
        val isCommentVisible: Boolean,
    ) : PhotoDetailEvent

    data object OnScrollCommentToTop : PhotoDetailEvent

    data class OnWriteComment(
        val comment: String,
    ) : PhotoDetailEvent

    data class OnEditComment(
        val commentId: Int,
        val comment: String,
    ) : PhotoDetailEvent

    data class OnDeleteComment(
        val commentId: Int,
    ) : PhotoDetailEvent

    data object OnDownloadPhoto : PhotoDetailEvent

    data object OnBack : PhotoDetailEvent

    data class OnShowSnackbar(
        val message: String,
    ) : PhotoDetailEvent
}
