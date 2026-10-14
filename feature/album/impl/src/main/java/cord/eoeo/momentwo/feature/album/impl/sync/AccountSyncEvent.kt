package cord.eoeo.momentwo.feature.album.impl.sync
import cord.eoeo.momentwo.core.common.UiEvent
import cord.eoeo.momentwo.core.model.SyncWork
internal sealed interface AccountSyncEvent:UiEvent {
    data object Back:AccountSyncEvent;data object Retry:AccountSyncEvent;data object Dismiss:AccountSyncEvent
    data class Toggle(val id:String):AccountSyncEvent;data object OpenCopy:AccountSyncEvent;data object ConfirmCopy:AccountSyncEvent
    data object OpenCreate:AccountSyncEvent;data class Text(val value:String):AccountSyncEvent;data object Create:AccountSyncEvent
    data class Link(val job:SyncWork):AccountSyncEvent;data class Inspect(val job:SyncWork):AccountSyncEvent;data class Bind(val id:Int):AccountSyncEvent
    data object Mine:AccountSyncEvent;data object Server:AccountSyncEvent;data object OpenRetry:AccountSyncEvent;data object RetryConfirmed:AccountSyncEvent
    data class OpenCancel(val job:SyncWork):AccountSyncEvent;data object Cancel:AccountSyncEvent
}
