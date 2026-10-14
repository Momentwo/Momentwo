package cord.eoeo.momentwo.feature.album.impl.account
import cord.eoeo.momentwo.core.common.UiError
internal sealed interface AccountLibraryAction {data class Snapshot(val content:AccountLibraryContent):AccountLibraryAction;data class Failure(val error:UiError):AccountLibraryAction;data object Refreshed:AccountLibraryAction;data object Refreshing:AccountLibraryAction;data class Pending(val key:String,val value:Boolean):AccountLibraryAction}
