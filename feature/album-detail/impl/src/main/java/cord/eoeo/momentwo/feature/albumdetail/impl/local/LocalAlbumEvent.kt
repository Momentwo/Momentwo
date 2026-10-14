package cord.eoeo.momentwo.feature.albumdetail.impl.local
import android.net.Uri
import cord.eoeo.momentwo.core.common.UiEvent
internal sealed interface LocalAlbumEvent : UiEvent {
    data object Retry : LocalAlbumEvent
    data class Open(val dialog: LocalAlbumDialog) : LocalAlbumEvent
    data object Dismiss : LocalAlbumEvent
    data class Text(val value: String) : LocalAlbumEvent
    data class Extra(val value: String) : LocalAlbumEvent
    data object Save : LocalAlbumEvent
    data object ConfirmDelete : LocalAlbumEvent
    data class Cover(val uri: Uri?) : LocalAlbumEvent
    data object Back : LocalAlbumEvent
    data object Connect : LocalAlbumEvent
}
