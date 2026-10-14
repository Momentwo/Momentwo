package cord.eoeo.momentwo.feature.albumdetail.impl
import cord.eoeo.momentwo.feature.albumdetail.impl.local.*
import org.junit.Test
import org.junit.Assert.*
class LocalAlbumSelectionTest {
    @Test fun responseSubtractsSubmittedIdsAndPreservesNewSelections() {
        val submitted=LocalAlbumState("album",editing=true,selected=setOf("old"),interaction=3)
        val newer=LocalAlbumReducer.reduce(submitted,LocalAlbumAction.Select("new",true))
        val result=LocalAlbumReducer.reduce(newer,LocalAlbumAction.Deleted(submitted.selected,submitted.interaction))
        assertEquals(setOf("new"),result.selected);assertTrue(result.editing);assertEquals(setOf("old"),submitted.selected)
    }
    @Test fun tabResponseDoesNotChangeActiveTabOrEditor() {
        val state=LocalAlbumState("album",tab=2,dialog=LocalAlbumDialog.EditSubtitle)
        val result=LocalAlbumReducer.reduce(state,LocalAlbumAction.Members(cord.eoeo.momentwo.core.common.ScreenState.Success(emptyList())))
        assertEquals(2,result.tab);assertEquals(LocalAlbumDialog.EditSubtitle,result.dialog)
    }
}
