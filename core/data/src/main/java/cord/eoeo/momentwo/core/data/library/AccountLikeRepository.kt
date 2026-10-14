package cord.eoeo.momentwo.core.data.library
import android.util.Log
import cord.eoeo.momentwo.core.common.runSuspendCatching
import cord.eoeo.momentwo.core.data.like.LikeRepository
import cord.eoeo.momentwo.core.data.like.remote.LikeService
import cord.eoeo.momentwo.core.data.model.LikeRequest
import cord.eoeo.momentwo.core.database.MomentwoDatabase
import javax.inject.Inject
class AccountLikeRepository @Inject constructor(private val clients:AccountClientFactory,private val db:MomentwoDatabase):LikeRepository {
    private suspend fun write(albumId:Int,photoId:Int,liked:Boolean):Result<Unit> = runSuspendCatching {
        val session=clients.snapshot();val service=clients.client(session).create(LikeService::class.java)
        if(liked)service.postDoLike(LikeRequest(albumId,photoId))else service.postUndoLike(LikeRequest(albumId,photoId))
        runSuspendCatching {db.photoDao().updateIsLiked(photoId,liked,session.identity.accountId)}.onFailure {Log.e("AccountLike","Server committed; cached status will refresh later",it)}
        Unit
    }
    override suspend fun requestDoLike(albumId:Int,photoId:Int)=write(albumId,photoId,true)
    override suspend fun requestUndoLike(albumId:Int,photoId:Int)=write(albumId,photoId,false)
    override suspend fun getLikeCount(albumId:Int,photoId:Int)=runSuspendCatching{val session=clients.snapshot();clients.client(session).create(LikeService::class.java).getLikeCount(albumId,photoId).count}
}
