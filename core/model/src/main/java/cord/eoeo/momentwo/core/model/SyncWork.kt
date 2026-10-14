package cord.eoeo.momentwo.core.model
enum class SyncKind {CREATE_ALBUM,SET_COVER,CREATE_SUB,UPLOAD_PHOTO,SET_DESCRIPTION,EDIT_ALBUM_TITLE,SET_SUBTITLE,EDIT_SUB,DELETE_ALBUM,DELETE_SUB,DELETE_PHOTO,DO_LIKE,UNDO_LIKE,CREATE_COMMENT,EDIT_COMMENT,DELETE_COMMENT}
enum class SyncStatus {QUEUED,PREPARING,SENDING,AWAITING_ID,UNKNOWN,CONFLICT,AUTH_REQUIRED,PERMISSION_DENIED,NOT_FOUND,DONE,CANCELLED}
data class SyncWork(val id:String,val accountId:String,val batchId:String?,val targetId:String,val kind:SyncKind,val status:SyncStatus,val displayName:String,val desired:String,val remote:String?)
data class RemoteCandidate(val id:Int,val title:String,val imageUri:String="")
data class AccountComment(val localId:String,val remoteId:Int?,val author:String,val text:String,val date:String,val mine:Boolean)
