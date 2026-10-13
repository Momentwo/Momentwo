package cord.eoeo.momentwo.feature.friend.impl

import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.FriendRequestItem
import org.junit.Assert.*
import org.junit.Test

class FriendReducerTest {
    private val first = FriendRequestItem(1, "one", "", false)
    private val second = FriendRequestItem(2, "two", "", false)
    @Test fun responseUsesIdAfterReorderingAndIgnoresRemovedItem() {
        val state = FriendState(received = ScreenState.Success(FriendRequestsContent(listOf(second, first))))
        val updated = FriendReducer.reduce(state, FriendAction.Responded(1, true))
        val items = (updated.received as ScreenState.Success).content.requests
        assertFalse(items[0].isUpdated)
        assertTrue(items[1].isUpdated)
        val missing = FriendReducer.reduce(updated, FriendAction.Responded(99, false))
        assertEquals(items, (missing.received as ScreenState.Success).content.requests)
        assertEquals(1L, missing.friendListRevision)
        assertFalse((state.received as ScreenState.Success).content.requests[1].isUpdated)
    }
    @Test fun olderQueryCannotUndoCommittedResponse() {
        val state = FriendReducer.reduce(FriendState(), FriendAction.Responded(1, true))
        assertEquals(state, FriendReducer.reduce(state, FriendAction.RequestsLoaded(RequestTab.Received, FriendRequestsContent(listOf(first)), 0)))
    }
    @Test fun responseFromOtherTabPreservesCurrentTabAndDialog() {
        val state = FriendState(requestTab = RequestTab.Sent, isRequestDialogOpened = true)
        val updated = FriendReducer.reduce(state, FriendAction.RequestsLoaded(RequestTab.Received, FriendRequestsContent(listOf(first)), 0))
        assertEquals(RequestTab.Sent, updated.requestTab)
        assertTrue(updated.isRequestDialogOpened)
        assertEquals(ScreenState.Loading, updated.sent)
    }
    @Test fun refreshErrorKeepsExistingItems() {
        val original = FriendState(received = ScreenState.Success(FriendRequestsContent(listOf(first))))
        val started = FriendReducer.reduce(original, FriendAction.RequestsStarted(RequestTab.Received))
        val failed = FriendReducer.reduce(started, FriendAction.RequestsFailed(RequestTab.Received, UiError.Network))
        val content = failed.received as ScreenState.Success
        assertEquals(listOf(first), content.content.requests)
        assertEquals(RefreshState.Error(UiError.Network), content.refresh)
    }
    @Test fun sentRequestDoesNotCloseReopenedOrEditedDialog() {
        val state = FriendState(isRequestDialogOpened = true, dialogRevision = 2)
        assertTrue(FriendReducer.reduce(state, FriendAction.RequestSent(1)).isRequestDialogOpened)
        assertFalse(FriendReducer.reduce(state, FriendAction.RequestSent(2)).isRequestDialogOpened)
    }
}
