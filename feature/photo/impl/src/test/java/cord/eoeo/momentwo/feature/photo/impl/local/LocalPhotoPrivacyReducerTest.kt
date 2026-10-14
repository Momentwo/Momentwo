package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.LibraryDraft
import org.junit.Assert.*
import org.junit.Test
class LocalPhotoPrivacyReducerTest {
    @Test fun lostOwnershipClosesEditorAndClearsDisplayedPrivateDraftButRefreshFailurePreservesIt() {
        val state=LocalPhotoState("A",dialog=LocalPhotoDialog.Caption,draft=LibraryDraft("private","","token"))
        val failed=LocalPhotoReducer.reduce(state,LocalPhotoAction.Failed(UiError.Unknown))
        assertEquals(state.draft,failed.draft);assertEquals(state.dialog,failed.dialog)
        val unavailable=LocalPhotoReducer.reduce(state,LocalPhotoAction.Failed(UiError.NotFound))
        assertEquals(LocalPhotoDialog.None,unavailable.dialog);assertEquals("",unavailable.draft.text)
        assertTrue(unavailable.screen is ScreenState.Error)
    }
}
