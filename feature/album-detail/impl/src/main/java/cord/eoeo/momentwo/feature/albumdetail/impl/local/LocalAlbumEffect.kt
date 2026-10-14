package cord.eoeo.momentwo.feature.albumdetail.impl.local
import cord.eoeo.momentwo.core.common.UiEffect
internal sealed interface LocalAlbumEffect : UiEffect { data object Back : LocalAlbumEffect; data object Connect : LocalAlbumEffect; data class Message(val text: String) : LocalAlbumEffect }
