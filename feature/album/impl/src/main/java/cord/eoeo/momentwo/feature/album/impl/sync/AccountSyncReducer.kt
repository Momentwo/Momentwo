package cord.eoeo.momentwo.feature.album.impl.sync
import cord.eoeo.momentwo.core.common.*
internal object AccountSyncReducer:Reducer<AccountSyncState,AccountSyncAction>{override fun reduce(state:AccountSyncState,action:AccountSyncAction):AccountSyncState=when(action){
    is AccountSyncAction.Snapshot->{val before=(state.screen as? ScreenState.Success)?.content?.identity;val changed=before!=null && (before.accountId!=action.content.identity.accountId || before.epoch!=action.content.identity.epoch);state.copy(screen=ScreenState.Success(action.content),dialog=if(changed)SyncDialog.None else state.dialog,selected=if(changed)emptySet()else state.selected,draft=if(changed)""else state.draft)}
    is AccountSyncAction.Error->state.copy(screen=if(action.error==UiError.Unauthorized)ScreenState.Error(action.error)else state.screen.failRefresh(action.error),dialog=if(action.error==UiError.Unauthorized)SyncDialog.None else state.dialog,selected=if(action.error==UiError.Unauthorized)emptySet()else state.selected,draft=if(action.error==UiError.Unauthorized)""else state.draft)
    is AccountSyncAction.Select->state.copy(selected=if(action.id in state.selected)state.selected-action.id else state.selected+action.id,token=action.token)
    is AccountSyncAction.Open->state.copy(dialog=action.dialog,token=action.token,draft=if(action.dialog==SyncDialog.Create)"" else state.draft)
    is AccountSyncAction.Edited->state.copy(draft=action.text,token=action.token)
    AccountSyncAction.Dismiss->state.copy(dialog=SyncDialog.None)
    is AccountSyncAction.Candidates->state.copy(dialog=(state.dialog as? SyncDialog.Link)?.takeIf{it.job.id==action.id}?.copy(candidates=ScreenState.Success(action.value)) ?: state.dialog)
    is AccountSyncAction.Inspected->state.copy(dialog=(state.dialog as? SyncDialog.Inspect)?.takeIf{it.job.id==action.id}?.copy(value=ScreenState.Success(action.value)) ?: state.dialog)
    is AccountSyncAction.ReadFailed->state.copy(dialog=when(val dialog=state.dialog){is SyncDialog.Link->if(dialog.job.id==action.id)dialog.copy(candidates=ScreenState.Error(action.error))else dialog;is SyncDialog.Inspect->if(dialog.job.id==action.id)dialog.copy(value=ScreenState.Error(action.error))else dialog;SyncDialog.None,SyncDialog.Copy,SyncDialog.Create,is SyncDialog.Retry,is SyncDialog.Cancel->dialog})
    is AccountSyncAction.Finished->if(action.token==state.token)state.copy(dialog=SyncDialog.None,selected=emptySet())else state
    is AccountSyncAction.Pending->state.copy(pending=if(action.value)state.pending+action.key else state.pending-action.key)
}}
