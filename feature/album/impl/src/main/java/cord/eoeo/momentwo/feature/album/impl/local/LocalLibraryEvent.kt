package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.UiEvent
internal sealed interface LocalLibraryEvent : UiEvent {
    data object Retry : LocalLibraryEvent
    data object ConnectAccount : LocalLibraryEvent
    data object OpenCreate : LocalLibraryEvent
    data object Dismiss : LocalLibraryEvent
    data class TextChanged(val text: String) : LocalLibraryEvent
    data object Save : LocalLibraryEvent
    data class OpenAlbum(val id: String) : LocalLibraryEvent
}
