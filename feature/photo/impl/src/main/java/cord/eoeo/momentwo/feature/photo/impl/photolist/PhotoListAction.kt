package cord.eoeo.momentwo.feature.photo.impl.photolist

import androidx.paging.PagingData
import cord.eoeo.momentwo.core.common.UiError
import cord.eoeo.momentwo.core.model.PhotoItem
import kotlinx.coroutines.flow.Flow

internal sealed interface PhotoListAction {
    data object SourceStarted : PhotoListAction
    data class SourceReady(val source: Flow<PagingData<PhotoItem>>) : PhotoListAction
    data class SourceFailed(val error: UiError) : PhotoListAction
    data class MenuChanged(val expanded: Boolean) : PhotoListAction
    data class EditChanged(val enabled: Boolean) : PhotoListAction
    data object CancelEdit : PhotoListAction
    data class SelectionChanged(val selected: Boolean, val id: Int) : PhotoListAction
    data class DialogChanged(val open: Boolean) : PhotoListAction
    data object DialogEdited : PhotoListAction
    data class Renamed(val title: String, val revision: Long) : PhotoListAction
    data class Deleted(val ids: List<Int>, val session: Long) : PhotoListAction
    data class PendingChanged(val key: PhotoListWriteKey, val pending: Boolean) : PhotoListAction
    data class DirtyChanged(val dirty: Boolean) : PhotoListAction
}
