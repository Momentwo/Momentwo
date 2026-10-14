package cord.eoeo.momentwo.feature.album.impl.sync
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
import org.junit.Assert.*
import org.junit.Test
class AccountSyncReducerTest {
    private fun content(id:String="A",epoch:String="1")=AccountSyncContent(AccountIdentity(id,epoch,true),emptyList(),emptyList())
    @Test fun oldCompletionCannotEraseNewSelectionOrInput(){
        val state=AccountSyncState(screen=ScreenState.Success(content()),selected=setOf("new"),token="new",draft="latest",dialog=SyncDialog.Create)
        assertEquals(state,AccountSyncReducer.reduce(state,AccountSyncAction.Finished("old")))
    }
    @Test fun switchingAccountOrLoggingOutClearsPrivateSelectionAndDialog(){
        val state=AccountSyncState(screen=ScreenState.Success(content()),selected=setOf("local"),dialog=SyncDialog.Copy,draft="private")
        val next=AccountSyncReducer.reduce(state,AccountSyncAction.Snapshot(content("B")))
        assertEquals(SyncDialog.None,next.dialog);assertTrue(next.selected.isEmpty())
        val out=AccountSyncReducer.reduce(state,AccountSyncAction.Error(UiError.Unauthorized));assertEquals(SyncDialog.None,out.dialog);assertTrue(out.selected.isEmpty());assertEquals("",out.draft)
    }
    @Test fun unrelatedDatabaseSnapshotsPreserveActiveInput(){
        val state=AccountSyncState(screen=ScreenState.Success(content()),token="latest",draft="editing",dialog=SyncDialog.Create)
        val next=AccountSyncReducer.reduce(state,AccountSyncAction.Snapshot(content()))
        assertEquals("editing",next.draft);assertEquals("latest",next.token);assertEquals(SyncDialog.Create,next.dialog)
    }
}
