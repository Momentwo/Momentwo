package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.UiEffect
internal sealed interface LibraryCreateEffect : UiEffect { data object Back : LibraryCreateEffect; data object Connect : LibraryCreateEffect; data class Message(val text: String) : LibraryCreateEffect }
