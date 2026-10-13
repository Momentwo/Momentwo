package cord.eoeo.momentwo.feature.albumdetail.impl

import cord.eoeo.momentwo.core.common.Reducer

internal object AlbumSelectionReducer : Reducer<AlbumDetailState, SelectionAction> {
    override fun reduce(state: AlbumDetailState, action: SelectionAction): AlbumDetailState = when (action) {
        is SelectionAction.SubAlbum -> state.copy(selectedSubAlbumIds = state.selectedSubAlbumIds.select(action.id, action.selected))
        is SelectionAction.Member -> state.copy(selectedMemberIds = state.selectedMemberIds.select(action.id, action.selected))
        is SelectionAction.Friend -> state.copy(selectedFriendList = if (action.selected)
            (state.selectedFriendList + action.item).distinctBy { it.nickname } else state.selectedFriendList.filterNot { it.nickname == action.item.nickname },
            inviteRevision = state.inviteRevision + 1)
        SelectionAction.StartEdit -> state.copy(isEditMode = true, editRevision = state.editRevision + 1)
        SelectionAction.CancelEdit -> state.copy(isEditMode = false, editRevision = state.editRevision + 1,
            selectedSubAlbumIds = if (state.tab == DetailTab.SubAlbums) emptyList() else state.selectedSubAlbumIds,
            selectedMemberIds = if (state.tab == DetailTab.Members) emptyList() else state.selectedMemberIds)
    }
    private fun List<Int>.select(id: Int, selected: Boolean) = if (selected) (this + id).distinct() else filterNot { it == id }
}
