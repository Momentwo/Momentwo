package cord.eoeo.momentwo.ui.photodetail

import androidx.paging.PagingData
import cord.eoeo.momentwo.ui.UiEffect
import cord.eoeo.momentwo.ui.UiEvent
import cord.eoeo.momentwo.ui.UiState
import cord.eoeo.momentwo.core.model.CommentItem
import cord.eoeo.momentwo.core.model.DescriptionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class PhotoDetailContract {
    data class State(
        val albumId: Int = -1,
        val photoId: Int = -1,
        val photoUrl: String = "",
        val descriptionItem: DescriptionItem = DescriptionItem("", "", "", "", emptyList()),
        val likeCount: Int = 0,
        val likeLastUpdated: Long = 0L,
        val commentPagingData: Flow<PagingData<CommentItem>> = emptyFlow(),
        val isWriteDescMode: Boolean = false,
        val isMenuVisible: Boolean = true,
        val isDescriptionVisible: Boolean = false,
        val isCommentVisible: Boolean = false,
        val isLiked: Boolean = false,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val isError: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data object OnToggleIsMenuVisible : Event

        data object OnToggleIsLiked : Event

        data object OnGetLikeCount : Event

        data class OnChangeIsDescriptionVisible(
            val isDescriptionVisible: Boolean,
        ) : Event

        data class OnChangeIsWriteDescMode(
            val isWriteDescMode: Boolean,
        ) : Event

        data object OnGetDescription : Event

        data class OnWriteDescription(
            val title: String,
            val contents: String,
        ) : Event

        data class OnEditDescription(
            val title: String,
            val contents: String,
        ) : Event

        data object OnDeleteDescription : Event

        data class OnChangeIsCommentVisible(
            val isCommentVisible: Boolean,
        ) : Event

        data object OnScrollCommentToTop : Event

        data class OnWriteComment(
            val comment: String,
        ) : Event

        data class OnEditComment(
            val commentId: Int,
            val comment: String,
        ) : Event

        data class OnDeleteComment(
            val commentId: Int,
        ) : Event

        data object OnDownloadPhoto : Event

        data object OnBack : Event

        data class OnShowSnackbar(
            val message: String,
        ) : Event
    }

    sealed interface Effect : UiEffect {
        data object RefreshCommentPagingData : Effect

        data object ScrollCommentToBottom : Effect

        data object ScrollCommentToTop : Effect

        data class ToggleSystemBars(
            val isVisible: Boolean,
        ) : Effect

        data object PopBackStack : Effect

        data class ShowSnackbar(
            val message: String,
        ) : Effect
    }
}
