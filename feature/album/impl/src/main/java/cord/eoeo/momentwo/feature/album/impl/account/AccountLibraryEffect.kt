package cord.eoeo.momentwo.feature.album.impl.account
import cord.eoeo.momentwo.core.common.UiEffect
internal sealed interface AccountLibraryEffect:UiEffect{data object Login:AccountLibraryEffect;data object Back:AccountLibraryEffect;data object Friend:AccountLibraryEffect;data class Open(val id:String):AccountLibraryEffect;data class Message(val text:String):AccountLibraryEffect}
