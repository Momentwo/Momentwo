package cord.eoeo.momentwo.feature.album.impl
import cord.eoeo.momentwo.feature.album.impl.local.*
import cord.eoeo.momentwo.core.model.LibraryDraft
import org.junit.Test
import org.junit.Assert.*
class LibraryCreateReducerTest {
    @Test fun lateCompletionPreservesNewerTitleAndInvitations() {
        val first = LibraryCreateState(ready = true, draft = LibraryDraft("submitted", "", "first"), selected = listOf("old"), pending = true)
        val edited = LibraryCreateReducer.reduce(first, LibraryCreateAction.Edit(LibraryDraft("new", "", "second"), listOf("new friend")))
        val result = LibraryCreateReducer.reduce(edited, LibraryCreateAction.Reset("first"))
        assertEquals("new", result.draft.text); assertEquals(listOf("new friend"), result.selected)
        assertEquals("submitted", first.draft.text)
    }
    @Test fun restoringTwiceNeverReplacesCurrentDraft() {
        val state = LibraryCreateReducer.reduce(LibraryCreateState(), LibraryCreateAction.Restore(LibraryDraft("saved", "", "a"), null, listOf("friend")))
        assertEquals(state, LibraryCreateReducer.reduce(state, LibraryCreateAction.Restore(LibraryDraft("late", "", "b"), null, emptyList())))
    }
    @Test fun reauthSameAccountUpdatesEpochWithoutChangingDraftAndOtherAccountIsBlocked() {
        val draft=LibraryDraft("keep","","token")
        val a=cord.eoeo.momentwo.core.model.AccountIdentity("A","old",false)
        val state=LibraryCreateState(ready=true,account=true,identity=a,draft=draft,selected=listOf("friend"))
        val changed=LibraryCreateReducer.reduce(state,LibraryCreateAction.Owner(cord.eoeo.momentwo.core.model.AccountIdentity("B","b",true)))
        assertFalse(changed.ownerAvailable);assertEquals(a,changed.identity);assertEquals(draft,changed.draft)
        val newA=cord.eoeo.momentwo.core.model.AccountIdentity("A","new",true)
        val resumed=LibraryCreateReducer.reduce(changed,LibraryCreateAction.Owner(newA))
        assertTrue(resumed.ownerAvailable);assertEquals(newA,resumed.identity);assertEquals(draft,resumed.draft);assertEquals(listOf("friend"),resumed.selected)
    }
}
