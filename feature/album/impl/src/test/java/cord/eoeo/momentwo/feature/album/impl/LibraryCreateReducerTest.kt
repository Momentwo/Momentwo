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
}
