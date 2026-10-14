package cord.eoeo.momentwo.feature.albumdetail.impl.local
import cord.eoeo.momentwo.core.common.*
internal object LocalAlbumReducer : Reducer<LocalAlbumState, LocalAlbumAction> {
    override fun reduce(state: LocalAlbumState, action: LocalAlbumAction) = when (action) {
        is LocalAlbumAction.Tab -> state.copy(tab = action.index, coverPage = false, editing = false, interaction = state.interaction + 1)
        is LocalAlbumAction.Editing -> state.copy(editing = action.value, selected = if (action.value) state.selected else emptySet(), selectedMembers = if (action.value) state.selectedMembers else emptySet(), interaction = state.interaction + 1)
        is LocalAlbumAction.Select -> state.copy(selected = if (action.checked) state.selected + action.id else state.selected - action.id, interaction = state.interaction + 1)
        is LocalAlbumAction.SelectMember -> state.copy(selectedMembers = if (action.checked) state.selectedMembers + action.id else state.selectedMembers - action.id, interaction = state.interaction + 1)
        is LocalAlbumAction.Deleted -> state.copy(selected = state.selected - action.ids, editing = if (state.interaction == action.interaction) false else state.editing, dialog = if (state.dialog == LocalAlbumDialog.DeleteSelected) LocalAlbumDialog.None else state.dialog)
        is LocalAlbumAction.MembersRemoved -> state.copy(selectedMembers=state.selectedMembers-action.ids,editing=if(state.interaction==action.interaction)false else state.editing,dialog=if(state.dialog==LocalAlbumDialog.DeleteSelected)LocalAlbumDialog.None else state.dialog)
        is LocalAlbumAction.CoverPage -> state.copy(coverPage = action.open, coverDirty = false, coverDraft = null, interaction = state.interaction + 1)
        is LocalAlbumAction.CoverDraft -> state.copy(coverDirty = true, coverDraft = action.uri, interaction = state.interaction + 1)
        is LocalAlbumAction.Members -> state.copy(members = action.state)
        is LocalAlbumAction.Friends -> state.copy(friends = action.state)
        is LocalAlbumAction.InviteCheck -> state.copy(invited = if (action.checked) (state.invited + action.friend).distinctBy { it.nickname } else state.invited.filterNot { it.nickname == action.friend.nickname }, interaction=state.interaction+1)
        is LocalAlbumAction.Snapshot -> state.copy(screen = ScreenState.Success(action.content))
        is LocalAlbumAction.Failed -> if(action.error==UiError.NotFound) state.copy(screen=ScreenState.Error(action.error),dialog=LocalAlbumDialog.None,selected=emptySet(),selectedMembers=emptySet(),editing=false,coverPage=false,members=ScreenState.Loading,friends=ScreenState.Loading,invited=emptyList(),draft=cord.eoeo.momentwo.core.model.LibraryDraft("","",""),interaction=state.interaction+1) else state.copy(screen=state.screen.failRefresh(action.error))
        is LocalAlbumAction.Open -> state.copy(dialog = action.dialog, draft = action.draft, interaction = state.interaction + 1)
        is LocalAlbumAction.Restored -> if (state.interaction == 0L) state.copy(dialog = action.dialog, draft = action.draft) else state
        LocalAlbumAction.Dismiss -> state.copy(dialog = LocalAlbumDialog.None, interaction = state.interaction + 1)
        is LocalAlbumAction.Edited -> state.copy(draft = action.draft, interaction = state.interaction + 1)
        is LocalAlbumAction.Saved -> if (state.draft.token == action.token && state.interaction == action.interaction) state.copy(dialog = LocalAlbumDialog.None) else state
        is LocalAlbumAction.Pending -> state.copy(pending = if (action.value) state.pending + action.key else state.pending - action.key)
    }
}
