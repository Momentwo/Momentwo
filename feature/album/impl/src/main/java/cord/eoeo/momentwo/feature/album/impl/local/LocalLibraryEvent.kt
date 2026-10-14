package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.UiEvent
internal sealed interface LocalLibraryEvent: UiEvent { data object Retry: LocalLibraryEvent;data object ConnectAccount: LocalLibraryEvent;data class OpenAlbum(val id: String): LocalLibraryEvent }
