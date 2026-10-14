package cord.eoeo.momentwo.core.data.library

import cord.eoeo.momentwo.core.database.MomentwoDatabase
import cord.eoeo.momentwo.core.database.SyncJobEntity
import cord.eoeo.momentwo.core.data.library.sync.RoomSyncWorkStore
import cord.eoeo.momentwo.core.model.SyncStatus

/** Debug-only access to the real durable attempt boundary for device regression tests. */
object SyncTestHarness {
    suspend fun recover(db:MomentwoDatabase,owner:String) = RoomSyncWorkStore(db).recover(owner)
    suspend fun claim(db:MomentwoDatabase,job:SyncJobEntity,token:String) = RoomSyncWorkStore(db).claim(job,token)
    suspend fun sending(db:MomentwoDatabase,job:SyncJobEntity,token:String) = RoomSyncWorkStore(db).sending(job,token)
    suspend fun settle(db:MomentwoDatabase,job:SyncJobEntity,token:String,status:SyncStatus) = RoomSyncWorkStore(db).settle(job,token,status)
}
