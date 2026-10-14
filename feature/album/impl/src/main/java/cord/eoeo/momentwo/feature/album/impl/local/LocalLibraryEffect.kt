package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.UiEffect
internal sealed interface LocalLibraryEffect : UiEffect {
    data object Login : LocalLibraryEffect
    data class Album(val id: String) : LocalLibraryEffect
    data class Message(val text: String) : LocalLibraryEffect
}
