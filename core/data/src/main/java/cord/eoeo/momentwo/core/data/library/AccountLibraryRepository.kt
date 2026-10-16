package cord.eoeo.momentwo.core.data.library
import android.content.Context
import android.os.StatFs
import androidx.room.withTransaction
import cord.eoeo.momentwo.core.data.album.remote.AlbumService
import cord.eoeo.momentwo.core.database.*
import cord.eoeo.momentwo.core.datastore.PreferenceRepository
import cord.eoeo.momentwo.core.model.LibraryScope
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
class AccountLibraryRepository @Inject constructor(private val db:MomentwoDatabase,private val preferences:PreferenceRepository,private val clients:AccountClientFactory,@ApplicationContext private val context:Context,private val media:LocalMediaStore=LocalMediaStore(context,db)) : AccountLibraryAccess {
    override fun identities()=preferences.identities()
    override suspend fun logout()=preferences.clearSession()
    override suspend fun storage():Pair<Long,Long> = withContext(Dispatchers.IO) { File(context.filesDir,"library-media").listFiles().orEmpty().sumOf {it.length()} to StatFs(context.filesDir.path).availableBytes }
    override suspend fun refresh() {
        val session=clients.snapshot();clients.check(session)
        check(db.syncDao().jobs(session.identity.accountId).none{it.kind=="CREATE_ALBUM" && it.status !in setOf("DONE","CANCELLED")}){"새 앨범의 서버 연결을 먼저 확인해주세요"}
        val albums=clients.client(session).create(AlbumService::class.java).getAlbumList().albumList
        clients.check(session)
        db.withTransaction {
            val libraryId=LibraryScope.Account(session.identity.accountId).id
            db.libraryDao().insertLibrary(LibraryEntity(libraryId,session.identity.accountId,session.identity.accountId))
            albums.forEach { album ->
                val old=db.libraryDao().remoteAlbum(libraryId,album.id)
                if(old == null) db.libraryDao().insertAlbum(LocalAlbumEntity(UUID.randomUUID().toString(),libraryId,album.title,album.subTitle,remoteId=album.id,createdAt=System.currentTimeMillis(),remoteCoverUrl=album.profileImage,subsLoaded=false))
                else { val pending=db.syncDao().jobs(session.identity.accountId).filter{it.targetId==old.localId && it.status !in setOf("DONE","CANCELLED")}.map{it.kind}.toSet();db.libraryDao().updateAlbum(old.copy(title=if("EDIT_ALBUM_TITLE" in pending)old.title else album.title,subtitle=if("SET_SUBTITLE" in pending)old.subtitle else album.subTitle,remoteCoverUrl=album.profileImage)) }
                val stored=requireNotNull(db.libraryDao().remoteAlbum(libraryId,album.id));db.syncDao().putLink(SyncLinkEntity(stored.localId,session.identity.accountId,"ALBUM",null,album.id))
            }
        }
    }
    override suspend fun refreshPermission(id: String) {
        val album=requireNotNull(db.libraryDao().album(id));if(album.libraryId==LibraryScope.Device.id || album.remoteId==null)return
        val session=clients.snapshot();check(album.libraryId==LibraryScope.Account(session.identity.accountId).id);clients.check(session)
        val role=clients.client(session).create(AlbumService::class.java).getAlbumRole(requireNotNull(album.remoteId)).rules
        check(cord.eoeo.momentwo.core.model.MemberAuth.entries.any { it.roleString==role });clients.check(session)
        db.withTransaction { val latest=requireNotNull(db.libraryDao().album(id));db.libraryDao().updateAlbum(latest.copy(role=role)) }
    }
    override suspend fun members(id: String): List<cord.eoeo.momentwo.core.model.MemberItem> {
        refreshPermission(id)
        return memberCall(id) { service, remoteId -> service.getMemberList(remoteId).memberList.map { row ->
            check(cord.eoeo.momentwo.core.model.MemberAuth.entries.any { it.roleString==row.rules });row.mapToMemberItem()
        } }
    }
    override suspend fun inviteMembers(id: String, names: List<String>) = memberCall(id) { service, remoteId -> service.postInviteMembers(cord.eoeo.momentwo.core.data.model.InviteMembers(remoteId,names)) }
    override suspend fun kickMembers(id: String, ids: Set<Int>) = memberCall(id, managing=true) { service, remoteId -> service.deleteKickMembers(remoteId,ids.sorted().joinToString(",")) }
    override suspend fun leaveAlbum(id: String) {
        val expected=clients.snapshot()
        memberCall(id) { service, remoteId -> clients.check(expected);service.deleteExitMember(remoteId) }
        // The device original is a different UUID and remains untouched.
        db.withTransaction { clients.check(expected);db.libraryDao().deleteAlbum(id) }
    }
    private suspend fun <T> memberCall(id: String, managing: Boolean = false, request: suspend (cord.eoeo.momentwo.core.data.member.remote.MemberService,Int) -> T): T {
        val album=requireNotNull(db.libraryDao().album(id));check(album.libraryId!=LibraryScope.Device.id && !album.deleted)
        val session=clients.snapshot();check(album.libraryId==LibraryScope.Account(session.identity.accountId).id);clients.check(session)
        if(managing)check(album.role in setOf("ROLE_ALBUM_ADMIN","ROLE_ALBUM_SUB_ADMIN"))
        val result=request(clients.client(session).create(cord.eoeo.momentwo.core.data.member.remote.MemberService::class.java),requireNotNull(album.remoteId))
        clients.check(session);return result
    }

    override suspend fun refreshAlbum(id: String) {
        val album=requireNotNull(db.libraryDao().album(id)); if(album.libraryId==LibraryScope.Device.id) return
        val session=clients.snapshot();check(album.libraryId==LibraryScope.Account(session.identity.accountId).id);clients.check(session)
        val remoteId=album.remoteId ?: return
        val queued=db.syncDao().jobs(session.identity.accountId)
        if(queued.any{it.parentId==id && it.kind=="CREATE_SUB" && it.status !in setOf("DONE","CANCELLED")})return
        if(queued.any{job->job.batchId!=null && job.status !in setOf("DONE","CANCELLED") && db.syncDao().batch(requireNotNull(job.batchId))?.replicaIds?.split(",")?.contains(id)==true})return
        val transport=clients.client(session)
        val role=transport.create(AlbumService::class.java).getAlbumRole(remoteId).rules
        val subs=transport.create(cord.eoeo.momentwo.core.data.subalbum.remote.SubAlbumService::class.java).getSubAlbumList(remoteId).subAlbumList
        clients.check(session)
        db.withTransaction {
            subs.forEach { sub ->
                val old=db.libraryDao().remoteSub(id,sub.id)
                if(old==null) db.libraryDao().insertSubAlbum(LocalSubAlbumEntity(UUID.randomUUID().toString(),id,sub.title,remoteId=sub.id,createdAt=System.currentTimeMillis(),photosLoaded=false))
                else {val pending=db.syncDao().jobs(session.identity.accountId).any{it.targetId==old.localId && it.kind=="EDIT_SUB" && it.status !in setOf("DONE","CANCELLED")};db.libraryDao().updateSubAlbum(old.copy(title=if(pending)old.title else sub.title))}
                val stored=requireNotNull(db.libraryDao().remoteSub(id,sub.id));db.syncDao().putLink(SyncLinkEntity(stored.localId,session.identity.accountId,"SUB",id,sub.id))
            }
            val latest=requireNotNull(db.libraryDao().album(id));db.libraryDao().updateAlbum(latest.copy(role=role,subsLoaded=true))
        }
    }

    private suspend fun accountSub(id:String):Triple<LocalAlbumEntity,LocalSubAlbumEntity,cord.eoeo.momentwo.core.datastore.StoredSession>? {
        val sub=requireNotNull(db.libraryDao().subAlbum(id));val album=requireNotNull(db.libraryDao().album(sub.albumId))
        if(album.libraryId==LibraryScope.Device.id)return null
        val session=clients.snapshot();check(album.libraryId==LibraryScope.Account(session.identity.accountId).id);return Triple(album,sub,session)
    }
    override suspend fun refreshPhotos(id:String) {
        val(album,sub,session)=accountSub(id) ?: return;val albumId=album.remoteId ?: return;val subId=sub.remoteId ?: return
        val pending=db.syncDao().jobs(session.identity.accountId)
        if(pending.any{it.parentId==id && it.kind=="UPLOAD_PHOTO" && it.status !in setOf("DONE","CANCELLED")})return
        val service=clients.client(session).create(cord.eoeo.momentwo.core.data.photo.remote.PhotoService::class.java)
        var cursor=0;val seen=mutableSetOf<Int>();val found=mutableSetOf<Int>()
        while(true) {
            check(seen.add(cursor));val page=service.getPhotoPage(albumId,subId,100,cursor);clients.check(session)
            db.withTransaction {
                page.images.forEach{photo->
                    found+=photo.id
                    val old=db.libraryDao().remotePhoto(id,photo.id)
                    if(old==null)db.libraryDao().insertPhoto(LocalPhotoEntity(UUID.randomUUID().toString(),id,null,remoteUrl=photo.imageUrl,remoteId=photo.id,descriptionKnown=false,commentsLoaded=false,createdAt=System.currentTimeMillis()))
                    else db.libraryDao().updatePhoto(old.copy(remoteUrl=photo.imageUrl))
                    val stored=requireNotNull(db.libraryDao().remotePhoto(id,photo.id));db.syncDao().putLink(SyncLinkEntity(stored.localId,session.identity.accountId,"PHOTO",id,photo.id))
                }
                val latest=requireNotNull(db.libraryDao().subAlbum(id));db.libraryDao().updateSubAlbum(latest.copy(photosLoaded=true))
            }
            cursor=page.nextCursor ?: break
        }
        db.withTransaction{db.libraryDao().allPhotos(id).filter{it.remoteId!=null && it.remoteId !in found && it.mediaName==null && pending.none{job->job.targetId==it.localId && job.status !in setOf("DONE","CANCELLED")}}.forEach{db.libraryDao().deletePhoto(it.localId)}}
    }
    override suspend fun refreshPhoto(id:String) {
        val photo=requireNotNull(db.libraryDao().photo(id));val(album,sub,session)=accountSub(photo.subAlbumId) ?: return
        val albumId=album.remoteId ?: return;val photoId=photo.remoteId ?: return
        val transport=clients.client(session)
        val count=transport.create(cord.eoeo.momentwo.core.data.like.remote.LikeService::class.java).getLikeCount(albumId,photoId).count
        val description=try{transport.create(cord.eoeo.momentwo.core.data.description.remote.DescriptionService::class.java).getDescription(albumId,photoId).description}catch(error:retrofit2.HttpException){if(error.code()==404)"" else throw error}
        val nickname=requireNotNull(db.profileDao().getProfile(session.identity.accountId)).nickname
        val liked=transport.create(cord.eoeo.momentwo.core.data.photo.remote.PhotoService::class.java).getLikedPhotos(requireNotNull(sub.remoteId),photoId,photoId).likesList.any{it.photoId==photoId && it.nickname==nickname && it.likesStatus}
        clients.check(session)
        db.withTransaction{val current=requireNotNull(db.libraryDao().photo(id));val pending=db.syncDao().jobs(session.identity.accountId).filter{it.targetId==id && it.status !in setOf("DONE","CANCELLED")}.map{it.kind}.toSet()
            val localLike=pending.any{it in setOf("DO_LIKE","UNDO_LIKE")}
            db.libraryDao().updatePhoto(current.copy(description=if("SET_DESCRIPTION" in pending)current.description else description,descriptionKnown=true,isLiked=if(localLike)current.isLiked else liked,likeCount=if(localLike)current.likeCount else count,likesKnown=true))
        }
        val current=db.libraryDao().photo(id) ?: return
        if(current.mediaName==null && (current.cacheName==null || !java.io.File(context.cacheDir,"server-media/${current.cacheName}").isFile))cachePhoto(current,session)
    }
    private suspend fun cachePhoto(photo:LocalPhotoEntity,session:cord.eoeo.momentwo.core.datastore.StoredSession)=withContext(Dispatchers.IO) {
        val directory=java.io.File(context.cacheDir,"server-media");if(!directory.exists() && !directory.mkdirs())throw java.io.IOException("사진 캐시를 만들 수 없습니다")
        val name="${UUID.randomUUID()}.img";val target=java.io.File(directory,name);var accepted=false
        try {
            val client=clients.client(session).callFactory() as okhttp3.OkHttpClient
            val call=client.newCall(okhttp3.Request.Builder().url(photo.remoteUrl).build())
            val response=kotlinx.coroutines.suspendCancellableCoroutine<okhttp3.Response>{continuation->
                continuation.invokeOnCancellation{call.cancel()}
                call.enqueue(object:okhttp3.Callback{override fun onFailure(call:okhttp3.Call,error:java.io.IOException){if(continuation.isActive)continuation.resumeWith(Result.failure(error))};override fun onResponse(call:okhttp3.Call,response:okhttp3.Response){continuation.resume(response,onCancellation={_,value,_->value.close()})}})
            }
            response.use{check(it.isSuccessful);requireNotNull(it.body).byteStream().use{input->target.outputStream().use{out->val buffer=ByteArray(64*1024);while(true){kotlinx.coroutines.currentCoroutineContext().ensureActive();val size=input.read(buffer);if(size<0)break;out.write(buffer,0,size)};out.fd.sync()}}}
            val bounds=android.graphics.BitmapFactory.Options().apply{inJustDecodeBounds=true};android.graphics.BitmapFactory.decodeFile(target.path,bounds);check(bounds.outWidth>0 && bounds.outHeight>0)
            clients.check(session)
            db.withTransaction{val latest=db.libraryDao().photo(photo.localId);if(latest!=null && !latest.deleted && latest.remoteUrl==photo.remoteUrl){db.libraryDao().updatePhoto(latest.copy(cacheName=name));accepted=true}}
        }finally{if(!accepted)target.delete()}
    }
    override suspend fun refreshComments(id:String) {
        val photo=requireNotNull(db.libraryDao().photo(id));val(album,_,session)=accountSub(photo.subAlbumId) ?: return
        val albumId=album.remoteId ?: return;val photoId=photo.remoteId ?: return
        val service=clients.client(session).create(cord.eoeo.momentwo.core.data.comment.remote.CommentService::class.java)
        val nickname=requireNotNull(db.profileDao().getProfile(session.identity.accountId)).nickname
        var cursor=0;val seen=mutableSetOf<Int>()
        while(true){check(seen.add(cursor));val page=service.getCommentPage(albumId,photoId,100,cursor);clients.check(session)
            db.withTransaction{
                val existing=db.syncDao().allComments(id)
                page.commentsList.forEach{comment->val old=existing.find{it.remoteId==comment.id};val localId=old?.localId ?: UUID.randomUUID().toString();val dirty=db.syncDao().jobs(session.identity.accountId).any{it.targetId==localId && it.status !in setOf("DONE","CANCELLED")};if(!dirty)db.syncDao().putComment(AccountCommentEntity(localId,id,comment.id,comment.nickname,comment.comment,comment.date,old?.mine==true || comment.nickname==nickname,old?.revision ?: 0));db.syncDao().putLink(SyncLinkEntity(localId,session.identity.accountId,"COMMENT",id,comment.id))}
                val latest=requireNotNull(db.libraryDao().photo(id));db.libraryDao().updatePhoto(latest.copy(commentsLoaded=true,commentNextCursor=page.nextCursor))
            }
            cursor=page.nextCursor ?: break
        }
    }

}
