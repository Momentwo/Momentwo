package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.*
internal object LibraryCreateReducer : Reducer<LibraryCreateState, LibraryCreateAction> {
    override fun reduce(state: LibraryCreateState, action: LibraryCreateAction) = when (action) {
        LibraryCreateAction.RestoreFailed -> state.copy(restoreFailed=true)
        is LibraryCreateAction.Owner -> if(action.identity?.accountId==state.identity?.accountId)state.copy(identity=action.identity,ownerAvailable=action.identity!=null)else state.copy(ownerAvailable=false,inviteOpen=false,friends=ScreenState.Loading,temporary=emptyList())
        is LibraryCreateAction.Restore -> if (!state.ready) state.copy(ready = true, restoreFailed=false, draft = action.draft, identity = action.identity, selected = action.names) else state
        is LibraryCreateAction.Edit -> state.copy(draft = action.draft, selected = action.names, revision = state.revision + 1)
        is LibraryCreateAction.Pending -> state.copy(pending = action.value)
        is LibraryCreateAction.Invite -> state.copy(inviteOpen = action.open, temporary = state.selected)
        is LibraryCreateAction.Check -> state.copy(temporary = if (action.checked) (state.temporary + action.name).distinct() else state.temporary - action.name)
        is LibraryCreateAction.Friends -> state.copy(friends = ScreenState.Success(action.rows))
        LibraryCreateAction.FriendsFailed -> state.copy(friends = ScreenState.Error(UiError.Unknown))
        is LibraryCreateAction.Reset -> if (state.draft.token == action.token) state.copy(draft = state.draft.copy(text = "", extra = ""), selected = emptyList()) else state
    }
}
