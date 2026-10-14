package cord.eoeo.momentwo.feature.album.impl.sync
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
internal data class AccountSyncContent(val identity:AccountIdentity,val albums:List<LocalAlbum>,val jobs:List<SyncWork>)
internal sealed interface SyncDialog {
    data object None:SyncDialog
    data object Copy:SyncDialog
    data object Create:SyncDialog
    data class Link(val job:SyncWork,val candidates:ScreenState<List<RemoteCandidate>> = ScreenState.Loading):SyncDialog
    data class Inspect(val job:SyncWork,val value:ScreenState<String> = ScreenState.Loading):SyncDialog
    data class Retry(val job:SyncWork):SyncDialog
    data class Cancel(val job:SyncWork):SyncDialog
}
internal data class AccountSyncState(val screen:ScreenState<AccountSyncContent> = ScreenState.Loading,val selected:Set<String> = emptySet(),val token:String="",val draft:String="",val dialog:SyncDialog = SyncDialog.None,val pending:Set<String> = emptySet()):UiState
