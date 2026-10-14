package cord.eoeo.momentwo.feature.album.impl.account
import cord.eoeo.momentwo.core.common.UiEvent
internal sealed interface AccountLibraryEvent:UiEvent {data object Retry:AccountLibraryEvent;data object Login:AccountLibraryEvent;data object Logout:AccountLibraryEvent;data object Back:AccountLibraryEvent;data object Sync:AccountLibraryEvent;data object Friend:AccountLibraryEvent;data class Open(val id:String):AccountLibraryEvent}
