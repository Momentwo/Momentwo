package cord.eoeo.momentwo.core.data.library
import android.content.Context
import android.net.Uri
import cord.eoeo.momentwo.core.common.runSuspendCatching
import cord.eoeo.momentwo.core.common.dispatcher.IoDispatcher
import cord.eoeo.momentwo.core.database.MomentwoDatabase
import cord.eoeo.momentwo.core.data.friend.*
import cord.eoeo.momentwo.core.data.friend.local.FriendLocalDataSource
import cord.eoeo.momentwo.core.data.friend.remote.*
import cord.eoeo.momentwo.core.data.photo.*
import cord.eoeo.momentwo.core.data.photo.local.PhotoLocalDataSource
import cord.eoeo.momentwo.core.data.photo.remote.*
import cord.eoeo.momentwo.core.data.presigned.remote.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
class AccountFriendRepository @Inject constructor(private val clients: AccountClientFactory,private val db:MomentwoDatabase,@IoDispatcher private val dispatcher:CoroutineDispatcher):FriendRepository {
    private suspend fun delegate():FriendRepository {
        val session=clients.snapshot();val remote=FriendRemoteDataSource(clients.client(session).create(FriendService::class.java),dispatcher)
        val local=FriendLocalDataSource(db.friendDao(),db.friendRemoteKeyDao(),dispatcher,session.identity.accountId)
        return FriendRepositoryImpl(remote,local,FriendRemoteMediator(remote,local))
    }
    override suspend fun sendFriendRequest(nickname:String)=runSuspendCatching{delegate().sendFriendRequest(nickname).getOrThrow()}
    override suspend fun responseFriendRequest(nickname:String,isAccepted:Boolean)=runSuspendCatching{delegate().responseFriendRequest(nickname,isAccepted).getOrThrow()}
    override suspend fun cancelFriendRequest(userId:Int)=runSuspendCatching{delegate().cancelFriendRequest(userId).getOrThrow()}
    override suspend fun getFriendPage(size:Int)=delegate().getFriendPage(size)
    override suspend fun getFriendList()=runSuspendCatching{delegate().getFriendList().getOrThrow()}
    override suspend fun getSentRequests()=runSuspendCatching{delegate().getSentRequests().getOrThrow()}
    override suspend fun getReceivedRequests()=runSuspendCatching{delegate().getReceivedRequests().getOrThrow()}
    override suspend fun getSearchUsers(nickname:String,size:Int)=delegate().getSearchUsers(nickname,size)
}
class AccountPhotoRepository @Inject constructor(private val clients:AccountClientFactory,private val db:MomentwoDatabase,@ApplicationContext private val context:Context,@IoDispatcher private val dispatcher:CoroutineDispatcher):PhotoRepository {
    private suspend fun delegate():PhotoRepository {
        val session=clients.snapshot();val retrofit=clients.client(session)
        val remote=PhotoRemoteDataSource(retrofit.create(PhotoService::class.java),dispatcher)
        val local=PhotoLocalDataSource(db.photoDao(),db.photoRemoteKeyDao(),dispatcher,db,session.identity.accountId)
        val upload=PresignedRemoteDataSource(retrofit.create(PresignedService::class.java),dispatcher)
        return PhotoRepositoryImpl(remote,local,upload,PhotoRemoteMediator.Factory(remote,local),context,dispatcher)
    }
    override suspend fun getPhotoPagingData(pageSize:Int,albumId:Int,subAlbumId:Int)=delegate().getPhotoPagingData(pageSize,albumId,subAlbumId)
    override suspend fun requestUploadPhoto(albumId:Int,subAlbumId:Int,image:Uri)=runSuspendCatching{delegate().requestUploadPhoto(albumId,subAlbumId,image).getOrThrow()}
    override suspend fun deletePhotos(albumId:Int,subAlbumId:Int,photoIds:List<Int>)=runSuspendCatching{delegate().deletePhotos(albumId,subAlbumId,photoIds).getOrThrow()}
    // The originating like RPC owns cache updates. A later callback has no account identity
    // and must not resolve the newly active account from the same server photo id.
    override suspend fun updateIsLiked(photoId:Int,isLiked:Boolean)=Unit
    override suspend fun downloadPhoto(imageUrl:String)=runSuspendCatching{delegate().downloadPhoto(imageUrl).getOrThrow()}
}
