package cord.eoeo.momentwo.core.data.library.sync
import cord.eoeo.momentwo.core.database.SyncJobEntity
import cord.eoeo.momentwo.core.model.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
class SyncEngineTest {
    private fun job(id:String="job",status:SyncStatus=SyncStatus.QUEUED,owner:String="A",dependency:String?=null)=SyncJobEntity(id,owner,null,id,null,SyncKind.CREATE_ALBUM.name,status.name,"name","name","",null,null,null,null,null,dependency,0,0,null)
    private class Store(initial:List<SyncJobEntity>):SyncWorkStore {
        val rows=initial.associateBy{it.id}.toMutableMap()
        override suspend fun recover(accountId:String){rows.values.toList().filter{it.accountId==accountId}.forEach{if(it.status==SyncStatus.SENDING.name)rows[it.id]=it.copy(status=SyncStatus.UNKNOWN.name,claimId=null)else if(it.status==SyncStatus.PREPARING.name)rows[it.id]=it.copy(status=SyncStatus.QUEUED.name,claimId=null)}}
        override suspend fun next(accountId:String)=rows.values.firstOrNull{it.accountId==accountId && it.status==SyncStatus.QUEUED.name && (it.dependency==null || rows[it.dependency]?.status==SyncStatus.DONE.name)}
        override suspend fun claim(job:SyncJobEntity,claim:String):Boolean {if(rows[job.id]?.status!=SyncStatus.QUEUED.name)return false;rows[job.id]=job.copy(status=SyncStatus.PREPARING.name,claimId=claim);return true}
        override suspend fun sending(job:SyncJobEntity,claim:String):Boolean {val row=rows.getValue(job.id);if(row.claimId!=claim||row.status!=SyncStatus.PREPARING.name)return false;rows[job.id]=row.copy(status=SyncStatus.SENDING.name,transferPhase=2);return true}
        override suspend fun settle(job:SyncJobEntity,claim:String,status:SyncStatus,remote:String?,extra:String?){val row=rows.getValue(job.id);if(row.claimId==claim)rows[job.id]=row.copy(status=status.name,claimId=null,remoteText=remote,remoteExtra=extra)}
        override suspend fun current(id:String)=rows[id]
        override suspend fun interrupted(job:SyncJobEntity,claim:String){val row=rows.getValue(job.id);if(row.claimId==claim)rows[job.id]=row.copy(status=if(row.status==SyncStatus.SENDING.name)SyncStatus.UNKNOWN.name else SyncStatus.QUEUED.name,claimId=null)}
    }
    private class Transport:SyncTransport {var prepare:suspend(SyncJobEntity)->Preparation={Preparation.Ready};var execute:suspend(SyncJobEntity)->WriteReceipt={WriteReceipt.Applied};var sends=0;override suspend fun prepare(job:SyncJobEntity)=prepare.invoke(job);override suspend fun execute(job:SyncJobEntity):WriteReceipt{sends++;return execute.invoke(job)}}
    @Test fun unknownWriteIsNeverReplayedAfterRecreation()=runTest{
        val store=Store(listOf(job()));val transport=Transport();transport.execute={throw IOException("response lost")}
        assertFalse(SyncEngine(store).run("A",transport));assertEquals(SyncStatus.UNKNOWN.name,store.rows.getValue("job").status)
        SyncEngine(store).run("A",transport);assertEquals(1,transport.sends)
    }
    @Test fun recoveryCannotGuessThatAStoredSendingRequestFailed()=runTest{
        val store=Store(listOf(job(status=SyncStatus.SENDING)));val transport=Transport();SyncEngine(store).run("A",transport)
        assertEquals(0,transport.sends);assertEquals(SyncStatus.UNKNOWN.name,store.rows.getValue("job").status)
    }
    @Test fun creationWithoutIdPausesDependentWorkAndOtherAccountsStayUntouched()=runTest{
        val store=Store(listOf(job(),job("child",dependency="job"),job("other",owner="B")));val transport=Transport();transport.execute={WriteReceipt.NeedsId}
        SyncEngine(store).run("A",transport);assertEquals(1,transport.sends);assertEquals(SyncStatus.AWAITING_ID.name,store.rows.getValue("job").status);assertEquals(SyncStatus.QUEUED.name,store.rows.getValue("child").status);assertEquals(SyncStatus.QUEUED.name,store.rows.getValue("other").status)
    }
    @Test fun conflictDoesNotSendAndTransientReadCanRetrySafely()=runTest{
        val store=Store(listOf(job()));val transport=Transport();transport.prepare={Preparation.Conflict("server")}
        SyncEngine(store).run("A",transport);assertEquals(0,transport.sends);assertEquals("server",store.rows.getValue("job").remoteText)
        val readStore=Store(listOf(job()));transport.prepare={throw IOException("offline")};assertTrue(SyncEngine(readStore).run("A",transport));assertEquals(SyncStatus.QUEUED.name,readStore.rows.getValue("job").status);assertEquals(0,transport.sends)
    }
    @Test fun cancellingAfterSendingCannotEmitSuccessOrEraseNewAttempt()=runTest{
        val store=Store(listOf(job()));val transport=Transport();val entered=CompletableDeferred<Unit>();transport.execute={entered.complete(Unit);awaitCancellation()}
        val run=launch{SyncEngine(store).run("A",transport)};entered.await();run.cancelAndJoin();assertEquals(SyncStatus.UNKNOWN.name,store.rows.getValue("job").status);assertNull(store.rows.getValue("job").claimId)
    }
    @Test fun cancellingBeforeSendingLeavesOnlySafeQueuedWork()=runTest{
        val store=Store(listOf(job()));val transport=Transport();val entered=CompletableDeferred<Unit>();transport.prepare={entered.complete(Unit);awaitCancellation()}
        val run=launch{SyncEngine(store).run("A",transport)};entered.await();run.cancelAndJoin();assertEquals(0,transport.sends);assertEquals(SyncStatus.QUEUED.name,store.rows.getValue("job").status)
    }
}
