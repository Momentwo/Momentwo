package cord.eoeo.momentwo.core.database
import androidx.room.*
@Entity(tableName="sync_batch")
data class SyncBatchEntity(@PrimaryKey val id:String,val accountId:String,val sourceIds:String,val replicaIds:String,val title:String,val createdAt:Long)
@Entity(tableName="sync_job",indices=[Index("accountId"),Index("batchId"),Index("targetId")])
data class SyncJobEntity(@PrimaryKey val id:String,val accountId:String,val batchId:String?,val targetId:String,val parentId:String?,val kind:String,val status:String,val displayName:String,val text:String="",val extra:String="",val mediaName:String?,val baseText:String?,val baseExtra:String?,val remoteText:String?,val remoteExtra:String?,val dependency:String?,val revision:Long,val createdAt:Long,val claimId:String?,val transferPhase:Int=0,val objectKey:String?=null)
@Entity(tableName="sync_link",indices=[Index(value=["accountId","kind","parentId","remoteId"])])
data class SyncLinkEntity(@PrimaryKey val localId:String,val accountId:String,val kind:String,val parentId:String?,val remoteId:Int?)
@Entity(tableName="sync_media_pin",primaryKeys=["jobId","mediaName"],foreignKeys=[ForeignKey(entity=SyncJobEntity::class,parentColumns=["id"],childColumns=["jobId"],onDelete=ForeignKey.CASCADE)])
data class SyncMediaPinEntity(val jobId:String,val mediaName:String)
@Entity(tableName="account_comment",foreignKeys=[ForeignKey(entity=LocalPhotoEntity::class,parentColumns=["localId"],childColumns=["photoId"],onDelete=ForeignKey.CASCADE)],indices=[Index("photoId")])
data class AccountCommentEntity(@PrimaryKey val localId:String,val photoId:String,val remoteId:Int?,val author:String,val text:String,val date:String,val mine:Boolean,val revision:Long=0,@ColumnInfo(defaultValue="0") val deleted:Boolean=false)
