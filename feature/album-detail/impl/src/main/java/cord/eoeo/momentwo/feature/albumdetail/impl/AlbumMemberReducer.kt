package cord.eoeo.momentwo.feature.albumdetail.impl

import cord.eoeo.momentwo.core.common.*

internal object AlbumMemberReducer : Reducer<AlbumDetailState, MemberAction> {
    override fun reduce(state: AlbumDetailState, action: MemberAction): AlbumDetailState = when (action) {
        MemberAction.Started -> state.copy(members = state.members.startRefresh())
        is MemberAction.Loaded -> if (state.memberVersion == action.version) state.copy(members = ScreenState.Success(action.content)) else state
        is MemberAction.Failed -> state.copy(members = state.members.failRefresh(action.error))
        MemberAction.Finished -> state.copy(members = state.members.cancelRefresh())
        MemberAction.Committed -> state.copy(memberVersion = state.memberVersion + 1)
        MemberAction.OpenInvite -> state.copy(isInviteDialogOpened = true, inviteRevision = state.inviteRevision + 1)
        MemberAction.DismissInvite -> state.copy(isInviteDialogOpened = false, selectedFriendList = emptyList(), inviteRevision = state.inviteRevision + 1)
        is MemberAction.Invited -> {
            val remaining = state.selectedFriendList.filterNot { it.nickname in action.names }
            state.copy(selectedFriendList = remaining, memberVersion = state.memberVersion + 1,
                isInviteDialogOpened = if (state.inviteRevision == action.revision && remaining.isEmpty()) false else state.isInviteDialogOpened)
        }
        is MemberAction.Kicked -> {
            val remaining = state.selectedMemberIds - action.ids.toSet()
            val content = when (val current = state.members) {
                ScreenState.Loading -> current
                is ScreenState.Error -> current
                is ScreenState.Success -> current.copy(content = current.content.copy(items = current.content.items.filterNot { it.id in action.ids }))
            }
            state.copy(members = content, selectedMemberIds = remaining, memberVersion = state.memberVersion + 1,
                isEditMode = if (state.editRevision == action.editRevision && remaining.isEmpty()) false else state.isEditMode)
        }
    }
}
