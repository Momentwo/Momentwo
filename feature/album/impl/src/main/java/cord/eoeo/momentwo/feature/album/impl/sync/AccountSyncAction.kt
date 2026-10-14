package cord.eoeo.momentwo.feature.album.impl.sync
import cord.eoeo.momentwo.core.model.RemoteCandidate
import cord.eoeo.momentwo.core.common.UiError
internal sealed interface AccountSyncAction {
    data class Snapshot(val content:AccountSyncContent):AccountSyncAction;data class Error(val error:UiError):AccountSyncAction
    data class Select(val id:String,val token:String):AccountSyncAction;data class Open(val dialog:SyncDialog,val token:String):AccountSyncAction
    data class Edited(val text:String,val token:String):AccountSyncAction;data object Dismiss:AccountSyncAction
    data class Candidates(val id:String,val value:List<RemoteCandidate>):AccountSyncAction;data class Inspected(val id:String,val value:String):AccountSyncAction
    data class ReadFailed(val id:String,val error:UiError):AccountSyncAction;data class Finished(val token:String):AccountSyncAction
    data class Pending(val key:String,val value:Boolean):AccountSyncAction
}
