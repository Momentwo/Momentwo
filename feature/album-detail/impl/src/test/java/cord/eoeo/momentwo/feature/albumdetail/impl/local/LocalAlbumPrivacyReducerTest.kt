package cord.eoeo.momentwo.feature.albumdetail.impl.local
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.LibraryDraft
import org.junit.Assert.*
import org.junit.Test
class LocalAlbumPrivacyReducerTest {
    @Test fun lostOwnershipClosesEditorAndClearsDisplayedPrivateDraftButRefreshFailurePreservesIt() {
        val state=LocalAlbumState("A",dialog=LocalAlbumDialog.EditAlbum,draft=LibraryDraft("private","","token"))
        val failed=LocalAlbumReducer.reduce(state,LocalAlbumAction.Failed(UiError.Unknown))
        assertEquals(state.draft,failed.draft);assertEquals(state.dialog,failed.dialog)
        val unavailable=LocalAlbumReducer.reduce(state,LocalAlbumAction.Failed(UiError.NotFound))
        assertEquals(LocalAlbumDialog.None,unavailable.dialog);assertEquals("",unavailable.draft.text)
        assertTrue(unavailable.screen is ScreenState.Error)
    }
}
