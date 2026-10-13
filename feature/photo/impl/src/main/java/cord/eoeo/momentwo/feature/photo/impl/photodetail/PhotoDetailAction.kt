package cord.eoeo.momentwo.feature.photo.impl.photodetail

import androidx.paging.PagingData
import cord.eoeo.momentwo.core.common.UiError
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow

internal sealed interface PhotoDetailAction {
    data object Started : PhotoDetailAction
    data class Loaded(val description: DescriptionItem, val count: Int, val likeVersion: Long, val descriptionVersion: Long) : PhotoDetailAction
    data class Failed(val error: UiError) : PhotoDetailAction
    data object Finished : PhotoDetailAction
    data class PendingChanged(val key: PhotoWriteKey, val pending: Boolean) : PhotoDetailAction
}
internal sealed interface PhotoUiAction : PhotoDetailAction {
    data object ToggleMenu : PhotoUiAction
    data class DescriptionVisible(val visible: Boolean) : PhotoUiAction
    data class CommentVisible(val visible: Boolean) : PhotoUiAction
}
internal sealed interface PhotoLikeAction : PhotoDetailAction {
    data class Optimistic(val liked: Boolean, val count: Int, val timestamp: Long) : PhotoLikeAction
    data class Rollback(val liked: Boolean, val count: Int, val version: Long) : PhotoLikeAction
}
internal sealed interface PhotoDescriptionAction : PhotoDetailAction {
    data class Editing(val enabled: Boolean) : PhotoDescriptionAction
    data object DraftEdited : PhotoDescriptionAction
    data class Committed(val revision: Long) : PhotoDescriptionAction
    data class Deleted(val revision: Long) : PhotoDescriptionAction
}
internal sealed interface PhotoCommentAction : PhotoDetailAction {
    data class SourceReady(val source: Flow<PagingData<CommentItem>>) : PhotoCommentAction
    data class SourceFailed(val error: UiError) : PhotoCommentAction
    data class DraftChanged(val text: String) : PhotoCommentAction
    data class DialogOpened(val id: Int, val text: String) : PhotoCommentAction
    data object DialogDismissed : PhotoCommentAction
    data object DialogEdited : PhotoCommentAction
    data class Edited(val id: Int, val revision: Long) : PhotoCommentAction
    data class Sent(val revision: Long) : PhotoCommentAction
}
