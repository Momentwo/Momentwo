package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.*
internal object LocalPhotoListReducer : Reducer<LocalPhotoListState, LocalPhotoListAction> {
    override fun reduce(state: LocalPhotoListState, action: LocalPhotoListAction) = when(action) {
        LocalPhotoListAction.Refreshing -> state.copy(screen=state.screen.startRefresh())
        LocalPhotoListAction.Refreshed -> state.copy(screen=state.screen.cancelRefresh())
        is LocalPhotoListAction.Menu -> state.copy(menu=action.open)
        is LocalPhotoListAction.Edit -> state.copy(editing=action.enabled,menu=false,selected=if(action.enabled)state.selected else emptySet(),revision=state.revision+1)
        is LocalPhotoListAction.Select -> state.copy(selected=if(action.checked)state.selected+action.id else state.selected-action.id,revision=state.revision+1)
        is LocalPhotoListAction.Draft -> state.copy(draft=action.draft,rename=action.open,menu=false,revision=state.revision+1)
        LocalPhotoListAction.Dismiss -> state.copy(rename=false,confirmDelete=false,revision=state.revision+1)
        LocalPhotoListAction.ConfirmDelete -> state.copy(confirmDelete=true)
        is LocalPhotoListAction.Deleted -> state.copy(selected=state.selected-action.ids,editing=if(state.revision==action.revision)false else state.editing,confirmDelete=false)
        is LocalPhotoListAction.Saved -> if(state.draft.token==action.token)state.copy(rename=false)else state
        is LocalPhotoListAction.Pending -> state.copy(pending=if(action.value)state.pending+action.key else state.pending-action.key)
        is LocalPhotoListAction.Snapshot -> state.copy(screen = ScreenState.Success(action.content))
        is LocalPhotoListAction.Failed -> if(action.error==UiError.NotFound)state.copy(screen=ScreenState.Error(action.error),editing=false,rename=false,confirmDelete=false,menu=false,selected=emptySet(),draft=cord.eoeo.momentwo.core.model.LibraryDraft("","",""),revision=state.revision+1)else state.copy(screen=state.screen.failRefresh(action.error))
        is LocalPhotoListAction.Importing -> state.copy(importing = action.value)
    }
}
