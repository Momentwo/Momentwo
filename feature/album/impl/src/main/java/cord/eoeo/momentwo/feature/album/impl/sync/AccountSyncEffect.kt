package cord.eoeo.momentwo.feature.album.impl.sync
import cord.eoeo.momentwo.core.common.UiEffect
internal sealed interface AccountSyncEffect:UiEffect {data object Back:AccountSyncEffect;data class Album(val id:String):AccountSyncEffect;data class Message(val text:String):AccountSyncEffect}
