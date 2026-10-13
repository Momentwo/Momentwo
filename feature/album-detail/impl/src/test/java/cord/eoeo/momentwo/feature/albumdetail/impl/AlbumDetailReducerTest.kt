package cord.eoeo.momentwo.feature.albumdetail.impl

import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
import org.junit.Assert.*
import org.junit.Test

class AlbumDetailReducerTest {
    private fun state() = AlbumDetailState(AlbumHeader(1, "title", "subtitle", ""))
    @Test fun activeTabOwnsPermissionAndUnknownRoleIsNeverAdmin() {
        val state = state().copy(settings = ScreenState.Success(SettingsContent(MemberAuth.ADMIN)), selectedNavIndex = 1)
        assertNull(state.permission)
        assertEquals(ScreenState.Loading, state.screen)
    }
    @Test fun selectionsAreImmutableAndIdempotent() {
        val before = state()
        val action = SelectionAction.SubAlbum(true, 1)
        val once = AlbumDetailReducer.reduce(before, action)
        assertEquals(once, AlbumDetailReducer.reduce(once, action))
        assertTrue(before.selectedSubAlbumIds.isEmpty())
        assertEquals(listOf(1), once.selectedSubAlbumIds)
    }
    @Test fun deletionPreservesSelectionsAddedDuringRequest() {
        val before = state().copy(selectedSubAlbumIds = listOf(1, 2), isEditMode = true, editRevision = 3)
        val after = AlbumDetailReducer.reduce(before, SubAlbumAction.Deleted(listOf(1), 3))
        assertEquals(listOf(2), after.selectedSubAlbumIds)
        assertTrue(after.isEditMode)
    }
    @Test fun oldDeletionDoesNotExitReopenedEditSession() {
        val before = state().copy(isEditMode = true, editRevision = 4)
        assertTrue(AlbumDetailReducer.reduce(before, SubAlbumAction.Deleted(listOf(1), 3)).isEditMode)
    }
    @Test fun invitePreservesNewSelectionsAndNewDialogContext() {
        val first = FriendItem("first", "")
        val second = FriendItem("second", "")
        val before = state().copy(selectedFriendList = listOf(first, second), isInviteDialogOpened = true, inviteRevision = 2)
        val after = AlbumDetailReducer.reduce(before, MemberAction.Invited(setOf("first"), 1))
        assertEquals(listOf(second), after.selectedFriendList)
        assertTrue(after.isInviteDialogOpened)
    }
    @Test fun lateTitleSavePreservesSubtitleAndEditedDialog() {
        val before = state().copy(dialog = AlbumDetailDialog.EditSubtitle, dialogRevision = 2)
        val after = AlbumDetailReducer.reduce(before, SettingAction.TitleChanged("saved", 1))
        assertEquals("subtitle", after.albumItem.subTitle)
        assertEquals(AlbumDetailDialog.EditSubtitle, after.dialog)
        assertEquals("saved", after.albumItem.title)
    }
    @Test fun oldQueryCannotRestoreDeletedItems() {
        val before = state().copy(subAlbumVersion = 2)
        val old = SubAlbumAction.Loaded(SubAlbumsContent(MemberAuth.ADMIN, listOf(SubAlbumItem(1, "old", emptyList()))), 1)
        assertEquals(before, AlbumDetailReducer.reduce(before, old))
    }
    @Test fun refreshFailureKeepsRequiredContentAndSelection() {
        val before = state().copy(subAlbums = ScreenState.Success(SubAlbumsContent(MemberAuth.SUB_ADMIN, emptyList())), selectedSubAlbumIds = listOf(2))
        val after = AlbumDetailReducer.reduce(AlbumDetailReducer.reduce(before, SubAlbumAction.Started), SubAlbumAction.Failed(UiError.Network))
        assertEquals(MemberAuth.SUB_ADMIN, after.permission)
        assertEquals(listOf(2), after.selectedSubAlbumIds)
        assertEquals(RefreshState.Error(UiError.Network), (after.subAlbums as ScreenState.Success).refresh)
    }

    @Test fun olderImageSaveDoesNotCloseNewEditingSession() {
        val before = state().copy(isInChangeImage = true, imageRevision = 2)
        assertTrue(AlbumDetailReducer.reduce(before, SettingAction.ImageSaved(null, 1)).isInChangeImage)
        assertFalse(AlbumDetailReducer.reduce(before, SettingAction.ImageSaved(null, 2)).isInChangeImage)
    }
}
