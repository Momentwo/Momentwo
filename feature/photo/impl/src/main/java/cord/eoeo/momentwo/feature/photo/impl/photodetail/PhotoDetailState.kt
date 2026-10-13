package cord.eoeo.momentwo.feature.photo.impl.photodetail

import androidx.paging.PagingData
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal sealed interface PhotoWriteKey : RequestKey {
    data object Like : PhotoWriteKey
    data object Description : PhotoWriteKey
    data object NewComment : PhotoWriteKey
    data class EditComment(val id: Int) : PhotoWriteKey
    data class DeleteComment(val id: Int) : PhotoWriteKey
    data object Download : PhotoWriteKey
}
internal sealed interface PhotoCommentDialog {
    data object None : PhotoCommentDialog
    data class Edit(val id: Int, val initialText: String) : PhotoCommentDialog
}
internal data class PhotoDetailContent(val description: DescriptionItem, val likeCount: Int, val isLiked: Boolean)
internal data class PhotoDetailState(
    val albumId: Int,
    val photoId: Int,
    val photoUrl: String,
    val initialIsLiked: Boolean,
    val screen: ScreenState<PhotoDetailContent> = ScreenState.Loading,
    val likeLastUpdated: Long? = null,
    val likeVersion: Long = 0,
    val descriptionVersion: Long = 0,
    val descriptionRevision: Long = 0,
    val descriptionSession: Long = 0,
    val confirmedDescriptionRevision: Long? = null,
    val commentDraft: String = "",
    val commentRevision: Long = 0,
    val commentDialog: PhotoCommentDialog = PhotoCommentDialog.None,
    val commentDialogRevision: Long = 0,
    val commentDialogSession: Long = 0,
    val commentPagingData: Flow<PagingData<CommentItem>> = emptyFlow(),
    val commentsReady: Boolean = false,
    val commentsError: UiError? = null,
    val isWriteDescMode: Boolean = false,
    val isMenuVisible: Boolean = true,
    val isDescriptionVisible: Boolean = false,
    val isCommentVisible: Boolean = false,
    val pendingWrites: Set<PhotoWriteKey> = emptySet(),
) : UiState {
    val barsVisible: Boolean get() = screen !is ScreenState.Success || isMenuVisible
}
