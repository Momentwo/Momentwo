package cord.eoeo.momentwo.core.database
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao interface SyncDao {
    @Query("SELECT * FROM sync_job WHERE accountId=:accountId ORDER BY createdAt,id") fun observeJobs(accountId:String):Flow<List<SyncJobEntity>>
    @Query("SELECT * FROM sync_job WHERE accountId=:accountId ORDER BY createdAt,id") suspend fun jobs(accountId:String):List<SyncJobEntity>
    @Query("SELECT j.* FROM sync_job j JOIN sync_batch b ON j.batchId=b.id WHERE instr(',' || b.replicaIds || ',', ',' || :rootId || ',') > 0") fun copyJobs(rootId:String):Flow<List<SyncJobEntity>>
    @Query("SELECT * FROM sync_job WHERE id=:id") suspend fun job(id:String):SyncJobEntity?
    @Query("SELECT * FROM sync_batch WHERE id=:id") suspend fun batch(id:String):SyncBatchEntity?
    @Insert suspend fun insertBatch(batch:SyncBatchEntity)
    @Insert suspend fun insertJob(job:SyncJobEntity)
    @Update suspend fun updateJob(job:SyncJobEntity)
    @Query("UPDATE sync_job SET status=:next,claimId=:claim WHERE id=:id AND status=:expected") suspend fun transition(id:String,expected:String,next:String,claim:String?):Int
    @Query("SELECT * FROM sync_link WHERE localId=:id") suspend fun link(id:String):SyncLinkEntity?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putLink(link:SyncLinkEntity)
    @Query("SELECT * FROM sync_link WHERE accountId=:accountId AND kind=:kind AND remoteId=:remoteId") suspend fun linked(accountId:String,kind:String,remoteId:Int):List<SyncLinkEntity>
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun pin(pin:SyncMediaPinEntity)
    @Query("DELETE FROM sync_media_pin WHERE jobId=:id") suspend fun unpin(id:String)
    @Query("SELECT * FROM account_comment WHERE photoId=:photoId AND deleted=0 ORDER BY date,localId") fun comments(photoId:String):Flow<List<AccountCommentEntity>>
    @Query("SELECT * FROM account_comment WHERE photoId=:photoId ORDER BY date,localId") suspend fun allComments(photoId:String):List<AccountCommentEntity>
    @Query("SELECT * FROM account_comment WHERE localId=:id") suspend fun comment(id:String):AccountCommentEntity?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun putComment(comment:AccountCommentEntity)
    @Query("DELETE FROM account_comment WHERE localId=:id") suspend fun deleteComment(id:String)
}
