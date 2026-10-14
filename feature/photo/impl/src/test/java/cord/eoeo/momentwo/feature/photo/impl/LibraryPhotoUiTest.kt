package cord.eoeo.momentwo.feature.photo.impl
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.feature.photo.impl.local.*
import org.junit.Test
import org.junit.Assert.*
class LibraryPhotoUiTest {
    @Test fun firstLineIsTitleAndAllRemainingLinesAreBody() {
        assertEquals(PhotoDescription("single",""),PhotoDescription.read("single"))
        val raw="title\nfirst\n\nlast"
        assertEquals(PhotoDescription("title","first\n\nlast"),PhotoDescription.read(raw))
        assertEquals(raw,PhotoDescription.read(raw).encode())
        assertEquals("",PhotoDescription.read("").encode())
    }
    @Test fun bulkPhotoResponsePreservesNewSelectionAndRenameDraft() {
        val state=LocalPhotoListState("sub",editing=true,selected=setOf("old"),draft=LibraryDraft("draft","","token"),revision=3)
        val newer=LocalPhotoListReducer.reduce(state,LocalPhotoListAction.Select("new",true))
        val result=LocalPhotoListReducer.reduce(newer,LocalPhotoListAction.Deleted(state.selected,state.revision))
        assertEquals(setOf("new"),result.selected);assertTrue(result.editing);assertEquals("draft",result.draft.text)
    }
    @Test fun refreshCannotResetPhotoSheetOrLatestCaptionInput() {
        val initial=LocalPhotoState("photo",sheet=LocalPhotoSheet.Description,dialog=LocalPhotoDialog.Caption,draft=LibraryDraft("new","body","token"))
        val photo=LocalPhoto("photo","sub","uri",null,null,"old",true,false,false,null,0)
        val result=LocalPhotoReducer.reduce(initial,LocalPhotoAction.Snapshot(LocalPhotoContent(photo,emptyList())))
        assertEquals(initial.draft,result.draft);assertEquals(initial.sheet,result.sheet)
        assertEquals(LocalPhotoDialog.Caption,result.dialog)
    }
}
