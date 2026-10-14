package cord.eoeo.momentwo.core.data.library.sync

import android.graphics.BitmapFactory
import androidx.room.withTransaction
import cord.eoeo.momentwo.core.data.album.remote.AlbumService
import cord.eoeo.momentwo.core.data.subalbum.remote.SubAlbumService
import cord.eoeo.momentwo.core.data.photo.remote.PhotoService
import cord.eoeo.momentwo.core.data.description.remote.DescriptionService
import cord.eoeo.momentwo.core.data.like.remote.LikeService
import cord.eoeo.momentwo.core.data.comment.remote.CommentService
import cord.eoeo.momentwo.core.data.presigned.remote.PresignedService
import cord.eoeo.momentwo.core.data.model.*
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.database.*
import cord.eoeo.momentwo.core.datastore.StoredSession
import cord.eoeo.momentwo.core.model.*
import java.io.IOException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

internal class AccountSyncTransport(private val session:StoredSession,private val db:MomentwoDatabase,private val clients:AccountClientFactory,private val media:LocalMediaStore):SyncTransport {
    private val retrofit=clients.client(session)
    private val albums=retrofit.create(AlbumService::class.java)
    private val subs=retrofit.create(SubAlbumService::class.java)
    private val photos=retrofit.create(PhotoService::class.java)
    private val descriptions=retrofit.create(DescriptionService::class.java)
    private val likes=retrofit.create(LikeService::class.java)
    private val comments=retrofit.create(CommentService::class.java)
    private val uploads=retrofit.create(PresignedService::class.java)
    private data class Address(val album:Int?,val sub:Int?,val photo:Int?,val comment:Int?)
    private suspend fun address(job:SyncJobEntity):Address {
        val link=db.syncDao().link(job.targetId)
        check(link==null || link.accountId==job.accountId)
        return when(link?.kind) {
            "ALBUM"->Address(link.remoteId,null,null,null)
            "SUB"->{val parent=db.syncDao().link(requireNotNull(link.parentId));check(parent?.accountId==job.accountId);Address(parent.remoteId,link.remoteId,null,null)}
            "PHOTO"->{val sub=db.syncDao().link(requireNotNull(link.parentId));val album=db.syncDao().link(requireNotNull(sub?.parentId));check(sub.accountId==job.accountId && album?.accountId==job.accountId);Address(album.remoteId,sub.remoteId,link.remoteId,null)}
            "COMMENT"->{val photo=db.syncDao().link(requireNotNull(link.parentId));val sub=db.syncDao().link(requireNotNull(photo?.parentId));val album=db.syncDao().link(requireNotNull(sub?.parentId));check(photo.accountId==job.accountId && sub.accountId==job.accountId && album?.accountId==job.accountId);Address(album.remoteId,sub.remoteId,photo.remoteId,link.remoteId)}
            null->Address(null,null,null,null)
            else->throw IOException("잘못된 연결 정보")
        }
    }
    private fun denied(code:Int):Nothing=throw HttpException(Response.error<Unit>(code,"Unavailable".toResponseBody()))
    override suspend fun prepare(job:SyncJobEntity):Preparation {
        check(job.accountId==session.identity.accountId);clients.check(session)
        val kind=SyncKind.valueOf(job.kind)
        if(kind==SyncKind.CREATE_ALBUM){albums.getAlbumList();return Preparation.Ready}
        val ids=address(job)
        val albumId=ids.album ?: return Preparation.Waiting
        val list=albums.getAlbumList().albumList
        val album=list.find{it.id==albumId}
        if(album==null) {if(kind==SyncKind.DELETE_ALBUM)return Preparation.AlreadyApplied;denied(404)}
        val permission=albums.getAlbumRole(albumId).also {check(it.albumId==albumId)}.rules
        val administration=kind in setOf(SyncKind.SET_COVER,SyncKind.CREATE_SUB,SyncKind.EDIT_ALBUM_TITLE,SyncKind.SET_SUBTITLE,SyncKind.EDIT_SUB,SyncKind.DELETE_SUB)
        if(permission !in MemberAuth.entries.map{it.roleString})denied(403)
        if(kind==SyncKind.DELETE_ALBUM && permission!=MemberAuth.ADMIN.roleString)denied(403)
        if(administration && permission !in setOf(MemberAuth.ADMIN.roleString,MemberAuth.SUB_ADMIN.roleString))denied(403)
        if(kind==SyncKind.CREATE_SUB)return Preparation.Ready
        if(kind==SyncKind.CREATE_COMMENT)return if(ids.photo==null) Preparation.Waiting else Preparation.Ready
        if(kind==SyncKind.UPLOAD_PHOTO){if(ids.sub==null)return Preparation.Waiting;return Preparation.Ready}
        val value=when(kind){
            SyncKind.EDIT_ALBUM_TITLE->album.title
            SyncKind.SET_SUBTITLE->album.subTitle
            SyncKind.SET_COVER->album.profileImage
            SyncKind.EDIT_SUB->{val subId=ids.sub ?: return Preparation.Waiting;subs.getSubAlbumList(albumId).subAlbumList.find{it.id==subId}?.title ?: denied(404)}
            SyncKind.SET_DESCRIPTION->{val photoId=ids.photo ?: return Preparation.Waiting;if(allPhotos(albumId,requireNotNull(ids.sub)).none{it.id==photoId})denied(404);try{descriptions.getDescription(albumId,photoId).description}catch(error:HttpException){if(error.code()==404)"" else throw error}}
            SyncKind.DO_LIKE,SyncKind.UNDO_LIKE->{val photoId=ids.photo ?: return Preparation.Waiting;val nickname=requireNotNull(db.profileDao().getProfile(job.accountId)).nickname;photos.getLikedPhotos(requireNotNull(ids.sub),photoId,photoId).likesList.find{it.photoId==photoId && it.nickname==nickname}?.likesStatus?.toString() ?: "false"}
            SyncKind.EDIT_COMMENT->{val commentId=ids.comment ?: return Preparation.Waiting;allComments(albumId,requireNotNull(ids.photo)).find{it.id==commentId}?.comment ?: denied(404)}
            SyncKind.DELETE_ALBUM->return Preparation.Ready
            SyncKind.DELETE_SUB->{val subId=ids.sub ?: return Preparation.Waiting;if(subs.getSubAlbumList(albumId).subAlbumList.none{it.id==subId})return Preparation.AlreadyApplied;return Preparation.Ready}
            SyncKind.DELETE_PHOTO->{val photoId=ids.photo ?: return Preparation.Waiting;if(allPhotos(albumId,requireNotNull(ids.sub)).none{it.id==photoId})return Preparation.AlreadyApplied;return Preparation.Ready}
            SyncKind.DELETE_COMMENT->{val commentId=ids.comment ?: return Preparation.Waiting;if(allComments(albumId,requireNotNull(ids.photo)).none{it.id==commentId})return Preparation.AlreadyApplied;return Preparation.Ready}
            SyncKind.CREATE_ALBUM,SyncKind.CREATE_SUB,SyncKind.UPLOAD_PHOTO,SyncKind.CREATE_COMMENT->error("Handled above")
        }
        if(kind!=SyncKind.SET_COVER && value==job.text)return Preparation.AlreadyApplied
        return if(job.baseText!=null && value!=job.baseText) Preparation.Conflict(value) else Preparation.Ready
    }
    private suspend fun allPhotos(albumId:Int,subId:Int):List<PhotoInfo>{val result=mutableListOf<PhotoInfo>();val seen=mutableSetOf<Int>();var cursor=0;while(true){check(seen.add(cursor));val page=photos.getPhotoPage(albumId,subId,100,cursor);result+=page.images;cursor=page.nextCursor ?: break};return result.distinctBy{it.id}}
    private suspend fun allComments(albumId:Int,photoId:Int):List<Comment>{val result=mutableListOf<Comment>();val seen=mutableSetOf<Int>();var cursor=0;while(true){check(seen.add(cursor));val page=comments.getCommentPage(albumId,photoId,100,cursor);result+=page.commentsList;cursor=page.nextCursor ?: break};return result.distinctBy{it.id}}
    private suspend fun stagedObject(job:SyncJobEntity,albumId:Int,cover:Boolean):String {
        val current=requireNotNull(db.syncDao().job(job.id))
        if(current.transferPhase>=2 && current.objectKey!=null)return requireNotNull(current.objectKey)
        val file=media.file(requireNotNull(job.mediaName));check(file.isFile)
        val options=BitmapFactory.Options().apply{inJustDecodeBounds=true};BitmapFactory.decodeFile(file.path,options)
        val mime=options.outMimeType ?: throw IOException("사진을 읽을 수 없습니다")
        val request=PresignedRequest(albumId,mime.substringAfter('/'))
        val url=(if(cover)albums.postAlbumPresigned(request)else photos.postPhotoPresigned(request)).presignedUrl.toHttpUrl()
        require(url.isHttps && url.username.isEmpty() && url.password.isEmpty())
        val key=url.encodedPath.removePrefix("/");require(key.isNotBlank())
        db.withTransaction{val latest=requireNotNull(db.syncDao().job(job.id));check(latest.status==SyncStatus.SENDING.name);db.syncDao().updateJob(latest.copy(transferPhase=1,objectKey=key))}
        uploads.uploadImage(url.toString(),file.asRequestBody(mime.toMediaType()))
        currentCoroutineContext().ensureActive()
        db.withTransaction{val latest=requireNotNull(db.syncDao().job(job.id));check(latest.status==SyncStatus.SENDING.name);db.syncDao().updateJob(latest.copy(transferPhase=2,objectKey=key))}
        return key
    }
    override suspend fun execute(job:SyncJobEntity):WriteReceipt {
        clients.check(session);val ids=address(job)
        when(SyncKind.valueOf(job.kind)){
            SyncKind.CREATE_ALBUM->{albums.postCreateAlbum(CreateAlbumInfo(job.text,cord.eoeo.momentwo.core.data.library.AlbumInvitations.decode(job.extra)));return WriteReceipt.NeedsId}
            SyncKind.CREATE_SUB->{subs.postCreateSubAlbum(CreateSubAlbumInfo(requireNotNull(ids.album),job.text));return WriteReceipt.NeedsId}
            SyncKind.UPLOAD_PHOTO->{val album=requireNotNull(ids.album);photos.postPhotoUpload(UploadPhoto(album,requireNotNull(ids.sub),stagedObject(job,album,false)));return WriteReceipt.NeedsId}
            SyncKind.SET_COVER->{val album=requireNotNull(ids.album);if(job.mediaName==null)albums.deleteAlbumImage(album)else albums.putAlbumImage(AlbumImage(album,stagedObject(job,album,true)))}
            SyncKind.EDIT_ALBUM_TITLE->albums.putAlbumTitle(EditAlbumTitle(requireNotNull(ids.album),job.text))
            SyncKind.SET_SUBTITLE->{val album=requireNotNull(ids.album);if(job.text.isEmpty())albums.deleteAlbumSubTitle(album)else albums.putAlbumSubTitle(AlbumSubTitle(album,job.text))}
            SyncKind.EDIT_SUB->subs.putEditSubAlbum(EditSubAlbumInfo(requireNotNull(ids.album),requireNotNull(ids.sub),job.text))
            SyncKind.SET_DESCRIPTION->{val album=requireNotNull(ids.album);val photo=requireNotNull(ids.photo);val old=try{descriptions.getDescription(album,photo).description}catch(error:HttpException){if(error.code()==404)null else throw error};if(job.text.isEmpty()){if(old!=null)descriptions.deleteDescription(album,photo)}else if(old.isNullOrEmpty())descriptions.postCreateDescription(CreateDescription(album,photo,job.text))else descriptions.putEditDescription(EditDescription(album,photo,job.text))}
            SyncKind.DELETE_ALBUM->albums.deleteAlbum(requireNotNull(ids.album))
            SyncKind.DELETE_SUB->subs.deleteSubAlbums(requireNotNull(ids.album),requireNotNull(ids.sub).toString())
            SyncKind.DELETE_PHOTO->photos.deletePhotos(requireNotNull(ids.album),requireNotNull(ids.sub),requireNotNull(ids.photo).toString())
            SyncKind.DO_LIKE->likes.postDoLike(LikeRequest(requireNotNull(ids.album),requireNotNull(ids.photo)))
            SyncKind.UNDO_LIKE->likes.postUndoLike(LikeRequest(requireNotNull(ids.album),requireNotNull(ids.photo)))
            SyncKind.CREATE_COMMENT->{comments.postCreateComment(CreateComment(requireNotNull(ids.album),requireNotNull(ids.photo),job.text));return WriteReceipt.NeedsId}
            SyncKind.EDIT_COMMENT->comments.putEditComment(EditComment(requireNotNull(ids.album),requireNotNull(ids.comment),job.text))
            SyncKind.DELETE_COMMENT->comments.deleteComment(requireNotNull(ids.album),requireNotNull(ids.comment))
        }
        return WriteReceipt.Applied
    }
    suspend fun candidates(job:SyncJobEntity):List<RemoteCandidate> {
        clients.check(session);check(job.accountId==session.identity.accountId)
        val ids=address(job)
        val result=when(SyncKind.valueOf(job.kind)) {
            SyncKind.CREATE_ALBUM->albums.getAlbumList().albumList.map{RemoteCandidate(it.id,it.title,it.profileImage)}
            SyncKind.CREATE_SUB->subs.getSubAlbumList(requireNotNull(ids.album)).subAlbumList.map{RemoteCandidate(it.id,it.title)}
            SyncKind.UPLOAD_PHOTO->allPhotos(requireNotNull(ids.album),requireNotNull(ids.sub)).map{RemoteCandidate(it.id,"사진 ${it.id}",it.imageUrl)}
            SyncKind.CREATE_COMMENT->{val profile=requireNotNull(db.profileDao().getProfile(job.accountId));allComments(requireNotNull(ids.album),requireNotNull(ids.photo)).filter{it.nickname==profile.nickname}.map{RemoteCandidate(it.id,it.comment)}}
            SyncKind.SET_COVER,SyncKind.SET_DESCRIPTION,SyncKind.EDIT_ALBUM_TITLE,SyncKind.SET_SUBTITLE,SyncKind.EDIT_SUB,SyncKind.DELETE_ALBUM,SyncKind.DELETE_SUB,SyncKind.DELETE_PHOTO,SyncKind.DO_LIKE,SyncKind.UNDO_LIKE,SyncKind.EDIT_COMMENT,SyncKind.DELETE_COMMENT->error("Not a creation")
        }
        val own=requireNotNull(db.syncDao().link(job.targetId))
        return result.filter { candidate->db.syncDao().linked(job.accountId,own.kind,candidate.id).none{it.localId!=job.targetId && it.parentId==own.parentId} }
    }
    suspend fun verifyBinding(job:SyncJobEntity,candidateId:Int):Pair<RemoteCandidate,String?> {
        val candidate=candidates(job).singleOrNull{it.id==candidateId} ?: throw IOException("연결 가능한 서버 항목을 다시 확인해주세요")
        if(SyncKind.valueOf(job.kind)==SyncKind.CREATE_ALBUM) {
            val role=albums.getAlbumRole(candidateId);check(role.albumId==candidateId)
            if(role.rules !in setOf(MemberAuth.ADMIN.roleString,MemberAuth.SUB_ADMIN.roleString))denied(403)
            return candidate to role.rules
        }
        val root=requireNotNull(address(job).album);val role=albums.getAlbumRole(root);check(role.albumId==root)
        if(role.rules !in MemberAuth.entries.map{it.roleString})denied(403)
        return candidate to null
    }
    suspend fun inspect(job:SyncJobEntity):String = when(val prepared=prepare(job.copy(baseText="@inspect"))) {
        Preparation.AlreadyApplied->job.text
        is Preparation.Conflict->prepared.text
        Preparation.Waiting->throw IOException("상위 항목의 연결을 먼저 확인해주세요")
        Preparation.Ready->if(SyncKind.valueOf(job.kind) in setOf(SyncKind.DELETE_ALBUM,SyncKind.DELETE_SUB,SyncKind.DELETE_PHOTO,SyncKind.DELETE_COMMENT)) "서버에 아직 존재합니다" else throw IOException("서버 항목을 선택해 연결해주세요")
    }

}
