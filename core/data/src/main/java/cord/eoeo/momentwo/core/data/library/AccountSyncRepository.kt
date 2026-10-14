package cord.eoeo.momentwo.core.data.library
import androidx.room.withTransaction
import cord.eoeo.momentwo.core.database.*
import cord.eoeo.momentwo.core.datastore.PreferenceRepository
import cord.eoeo.momentwo.core.model.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.*
@Singleton
class AccountSyncRepository @Inject constructor(private val db:MomentwoDatabase,private val preferences:PreferenceRepository,private val media:LocalMediaStore,private val scheduler:SyncScheduling,private val clients:AccountClientFactory?=null) {
    private val dao get()=db.libraryDao()
    private val work get()=db.syncDao()
    suspend fun requireOwner(expected:AccountIdentity):AccountIdentity {
        val current=requireNotNull(preferences.getSession().getOrThrow()).identity
        check(current.accountId==expected.accountId && current.epoch==expected.epoch){"복사 대상 계정이 변경되었습니다"}
        return current
    }
    suspend fun currentOwner():AccountIdentity = requireNotNull(preferences.getSession().getOrThrow()).identity
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observeWorks()=preferences.identities().flatMapLatest { identity -> if(identity==null) flowOf(emptyList()) else work.observeJobs(identity.accountId).map { rows->rows.map { SyncWork(it.id,it.accountId,it.batchId,it.targetId,SyncKind.valueOf(it.kind),SyncStatus.valueOf(it.status),it.displayName,it.text,it.remoteText) } } }
    suspend fun copyAlbums(ids:Set<String>,expected:AccountIdentity,intentId:String):String {
        require(ids.isNotEmpty());requireOwner(expected)
        db.withTransaction {
            val existing=work.batch(intentId)
            if(existing!=null){check(existing.accountId==expected.accountId && existing.sourceIds==ids.sorted().joinToString(","));return@withTransaction}
            val source=ids.sorted().map { id->requireNotNull(dao.album(id)).also{check(it.libraryId==LibraryScope.Device.id)} }
            val libraryId=LibraryScope.Account(expected.accountId).id
            dao.insertLibrary(LibraryEntity(libraryId,expected.accountId,expected.accountId))
            val replicas=mutableListOf<String>()
            source.forEach { album ->
                album.coverMediaName?.let {check(media.file(it).isFile){"대표 사진 파일을 찾을 수 없습니다"}}
                val replica=UUID.randomUUID().toString();replicas+=replica
                dao.insertAlbum(album.copy(localId=replica,libraryId=libraryId,remoteId=null,role=null,revision=0,createdAt=System.currentTimeMillis(),remoteCoverUrl="",subsLoaded=true,locallyCreated=true))
                work.putLink(SyncLinkEntity(replica,expected.accountId,"ALBUM",null,null))
                val root=enqueue(expected.accountId,replica,null,SyncKind.CREATE_ALBUM,album.title,album.title,batchId=intentId)
                enqueue(expected.accountId,replica,null,SyncKind.SET_SUBTITLE,album.title,album.subtitle,base="",dependency=root,batchId=intentId)
                album.coverMediaName?.let {enqueue(expected.accountId,replica,null,SyncKind.SET_COVER,album.title,"",mediaName=it,base="",dependency=root,batchId=intentId)}
                dao.allSubs(album.localId).forEach { sub ->
                    val child=UUID.randomUUID().toString()
                    dao.insertSubAlbum(sub.copy(localId=child,albumId=replica,remoteId=null,revision=0,photosLoaded=true))
                    work.putLink(SyncLinkEntity(child,expected.accountId,"SUB",replica,null))
                    val subJob=enqueue(expected.accountId,child,replica,SyncKind.CREATE_SUB,"${album.title} / ${sub.title}",sub.title,dependency=root,batchId=intentId)
                    dao.allPhotos(sub.localId).forEach { photo ->
                        val name=requireNotNull(photo.mediaName){"기기에 보관된 사진만 복사할 수 있습니다"};check(media.file(name).isFile){"사진 파일을 찾을 수 없습니다"}
                        val image=UUID.randomUUID().toString()
                        // Private annotations are intentionally not part of an account copy.
                        dao.insertPhoto(photo.copy(localId=image,subAlbumId=child,remoteId=null,remoteUrl="",revision=0,favorite=false,isLiked=false,likeCount=null,likesKnown=false,commentsLoaded=false,cacheName=null))
                        work.putLink(SyncLinkEntity(image,expected.accountId,"PHOTO",child,null))
                        val upload=enqueue(expected.accountId,image,child,SyncKind.UPLOAD_PHOTO,"${album.title} / ${sub.title} / 사진","",mediaName=name,dependency=subJob,batchId=intentId)
                        enqueue(expected.accountId,image,child,SyncKind.SET_DESCRIPTION,"${album.title} / ${sub.title} / 사진 설명",photo.description,base="",dependency=upload,batchId=intentId)
                    }
                }
            }
            work.insertBatch(SyncBatchEntity(intentId,expected.accountId,ids.sorted().joinToString(","),replicas.joinToString(","),source.joinToString {it.title},System.currentTimeMillis()))
        }
        scheduler.enqueue()
        return intentId
    }
    /** Must be called inside the same transaction as the local mutation. */
    internal suspend fun enqueue(accountId:String,targetId:String,parentId:String?,kind:SyncKind,label:String,text:String,extra:String="",mediaName:String?=null,base:String?=null,baseExtra:String?=null,dependency:String?=null,batchId:String?=null,revision:Long=0):String {
        val id=UUID.randomUUID().toString()
        work.insertJob(SyncJobEntity(id,accountId,batchId,targetId,parentId,kind.name,SyncStatus.QUEUED.name,label,text,extra,mediaName,base,baseExtra,null,null,dependency,revision,maxOf(System.currentTimeMillis(),work.latestOrder()+1),null))
        if(mediaName!=null)work.pin(SyncMediaPinEntity(id,mediaName))
        return id
    }
    suspend fun schedulePending(){scheduler.enqueue()}
    internal suspend fun ownerFor(album:LocalAlbumEntity,kind:SyncKind?=null):AccountIdentity {
        val owner=currentOwner();check(album.libraryId==LibraryScope.Account(owner.accountId).id){"다른 계정의 자료입니다"};check(!album.deleted)
        val related=work.jobs(owner.accountId).filter{it.batchId!=null && it.status !in setOf(SyncStatus.DONE.name,SyncStatus.CANCELLED.name)}
        if(kind!=null) check(related.none{job->work.batch(requireNotNull(job.batchId))?.replicaIds?.split(",")?.contains(album.localId)==true}){"진행 중인 복사본은 완료 후 편집할 수 있습니다"}
        if(kind!=null && !(album.locallyCreated && album.remoteId==null)) {
            val roles=when(kind){SyncKind.DELETE_ALBUM->setOf(MemberAuth.ADMIN.roleString);SyncKind.SET_COVER,SyncKind.CREATE_SUB,SyncKind.EDIT_ALBUM_TITLE,SyncKind.SET_SUBTITLE,SyncKind.EDIT_SUB,SyncKind.DELETE_SUB->setOf(MemberAuth.ADMIN.roleString,MemberAuth.SUB_ADMIN.roleString);SyncKind.CREATE_ALBUM,SyncKind.UPLOAD_PHOTO,SyncKind.SET_DESCRIPTION,SyncKind.DELETE_PHOTO,SyncKind.DO_LIKE,SyncKind.UNDO_LIKE,SyncKind.CREATE_COMMENT,SyncKind.EDIT_COMMENT,SyncKind.DELETE_COMMENT->MemberAuth.entries.map{it.roleString}.toSet()}
            check(album.role in roles){"권한 정보를 확인한 뒤 사용할 수 있습니다"}
        }
        return owner
    }
    internal suspend fun mutation(owner:AccountIdentity,targetId:String,parentId:String?,kind:SyncKind,label:String,value:String,base:String?,revision:Long,mediaName:String?=null):String {
        val jobs=work.jobs(owner.accountId)
        val prior=jobs.lastOrNull{it.targetId==targetId && it.status !in setOf(SyncStatus.DONE.name,SyncStatus.CANCELLED.name)}
        if(prior?.status==SyncStatus.QUEUED.name && (prior.kind==kind.name || (prior.kind in setOf(SyncKind.DO_LIKE.name,SyncKind.UNDO_LIKE.name) && kind in setOf(SyncKind.DO_LIKE,SyncKind.UNDO_LIKE))) && prior.batchId==null){
            work.updateJob(prior.copy(kind=kind.name,text=value,revision=revision,mediaName=mediaName,transferPhase=if(prior.mediaName==mediaName)prior.transferPhase else 0,objectKey=if(prior.mediaName==mediaName)prior.objectKey else null));work.unpin(prior.id);if(mediaName!=null)work.pin(SyncMediaPinEntity(prior.id,mediaName));return prior.id
        }
        val parent=parentId?.let{id->jobs.lastOrNull{it.targetId==id && it.kind in setOf(SyncKind.CREATE_ALBUM.name,SyncKind.CREATE_SUB.name,SyncKind.UPLOAD_PHOTO.name) && it.status!=SyncStatus.DONE.name}}
        return enqueue(owner.accountId,targetId,parentId,kind,label,value,mediaName=mediaName,base=base,dependency=prior?.id ?: parent?.id,revision=revision)
    }
    suspend fun createAccountAlbum(title:String,intentId:String,expected:AccountIdentity?=null,inviteNames:List<String> = emptyList()):String {
        require(title.isNotBlank());val owner=if(expected==null)currentOwner()else requireOwner(expected);val libraryId=LibraryScope.Account(owner.accountId).id
        db.withTransaction {
            dao.insertLibrary(LibraryEntity(libraryId,owner.accountId,owner.accountId))
            val prior=dao.album(intentId)
            if(prior!=null){check(prior.libraryId==libraryId);return@withTransaction}
            dao.insertAlbum(LocalAlbumEntity(intentId,libraryId,title.trim(),createdAt=System.currentTimeMillis(),locallyCreated=true))
            work.putLink(SyncLinkEntity(intentId,owner.accountId,"ALBUM",null,null))
            enqueue(owner.accountId,intentId,null,SyncKind.CREATE_ALBUM,title,title.trim(),extra=AlbumInvitations.encode(inviteNames))
        }
        scheduler.enqueue();return intentId
    }
    fun comments(photoId:String):Flow<List<AccountComment>> = work.comments(photoId).map{rows->rows.map{AccountComment(it.localId,it.remoteId,it.author,it.text,it.date,it.mine)}}
    private suspend fun albumForPhoto(photoId:String):Pair<LocalAlbumEntity,LocalPhotoEntity> {val photo=requireNotNull(dao.photo(photoId));val sub=requireNotNull(dao.subAlbum(photo.subAlbumId));return requireNotNull(dao.album(sub.albumId)) to photo}
    suspend fun setLiked(photoId:String,liked:Boolean) {
        db.withTransaction {
            val (album,photo)=albumForPhoto(photoId);val kind=if(liked)SyncKind.DO_LIKE else SyncKind.UNDO_LIKE;val owner=ownerFor(album,kind)
            check(photo.likesKnown && photo.remoteId!=null && !photo.deleted)
            if(photo.isLiked==liked)return@withTransaction
            val next=photo.copy(isLiked=liked,likeCount=photo.likeCount?.let{(it+if(liked)1 else -1).coerceAtLeast(0)},revision=photo.revision+1)
            dao.updatePhoto(next);mutation(owner,photoId,photo.subAlbumId,kind,album.title+" / 좋아요",liked.toString(),photo.isLiked.toString(),next.revision)
        };scheduler.enqueue()
    }
    suspend fun saveComment(photoId:String,text:String,id:String) {
        require(text.isNotBlank())
        db.withTransaction {
            val (album,photo)=albumForPhoto(photoId);val prior=work.comment(id);val kind=if(prior==null)SyncKind.CREATE_COMMENT else SyncKind.EDIT_COMMENT;val owner=ownerFor(album,kind);check(!photo.deleted)
            val nickname=requireNotNull(db.profileDao().getProfile(owner.accountId)).nickname
            if(prior==null){work.putComment(AccountCommentEntity(id,photoId,null,nickname,text,System.currentTimeMillis().toString(),true));work.putLink(SyncLinkEntity(id,owner.accountId,"COMMENT",photoId,null));mutation(owner,id,photoId,kind,album.title+" / 댓글",text,null,0)}
            else{check(prior.mine && prior.photoId==photoId && !prior.deleted);val next=prior.copy(text=text,revision=prior.revision+1);work.putComment(next);mutation(owner,id,photoId,kind,album.title+" / 댓글",text,prior.text,next.revision)}
        };scheduler.enqueue()
    }
    suspend fun deleteComment(id:String) {
        db.withTransaction{val prior=requireNotNull(work.comment(id));check(prior.mine);val(album,_)=albumForPhoto(prior.photoId);val owner=ownerFor(album,SyncKind.DELETE_COMMENT);val next=prior.copy(deleted=true,revision=prior.revision+1);work.putComment(next);mutation(owner,id,prior.photoId,SyncKind.DELETE_COMMENT,album.title+" / 댓글 삭제","",prior.text,next.revision)};scheduler.enqueue()
    }

    private suspend fun confirmation(jobId:String,expected:AccountIdentity):Pair<SyncJobEntity,cord.eoeo.momentwo.core.data.library.sync.AccountSyncTransport> {
        requireOwner(expected);val job=requireNotNull(work.job(jobId));check(job.accountId==expected.accountId)
        val client=requireNotNull(clients){"계정 연결을 확인해주세요"};val session=client.snapshot();client.check(session)
        return job to cord.eoeo.momentwo.core.data.library.sync.AccountSyncTransport(session,db,client,media)
    }
    suspend fun candidates(jobId:String,expected:AccountIdentity):List<RemoteCandidate>{val(job,transport)=confirmation(jobId,expected);check(job.status in setOf(SyncStatus.AWAITING_ID.name,SyncStatus.UNKNOWN.name));return transport.candidates(job)}
    suspend fun inspect(jobId:String,expected:AccountIdentity):String {val(job,transport)=confirmation(jobId,expected);check(job.status in setOf(SyncStatus.UNKNOWN.name,SyncStatus.CONFLICT.name,SyncStatus.PERMISSION_DENIED.name,SyncStatus.NOT_FOUND.name));return transport.inspect(job)}
    suspend fun bind(jobId:String,candidateId:Int,expected:AccountIdentity) {
        val(job,transport)=confirmation(jobId,expected);check(job.status in setOf(SyncStatus.AWAITING_ID.name,SyncStatus.UNKNOWN.name))
        val(candidate,role)=transport.verifyBinding(job,candidateId);requireOwner(expected)
        db.withTransaction {
            val current=requireNotNull(work.job(jobId));check(current.status==job.status)
            val link=requireNotNull(work.link(job.targetId));check(link.accountId==expected.accountId)
            work.putLink(link.copy(remoteId=candidateId))
            when(SyncKind.valueOf(job.kind)) {
                SyncKind.CREATE_ALBUM->{val row=requireNotNull(dao.album(job.targetId));dao.updateAlbum(row.copy(remoteId=candidateId,role=role));if(candidate.title!=job.text)enqueue(job.accountId,job.targetId,null,SyncKind.EDIT_ALBUM_TITLE,job.displayName,job.text,base=job.text,dependency=job.id,batchId=job.batchId,revision=row.revision)}
                SyncKind.CREATE_SUB->{val row=requireNotNull(dao.subAlbum(job.targetId));dao.updateSubAlbum(row.copy(remoteId=candidateId));if(candidate.title!=job.text)enqueue(job.accountId,job.targetId,job.parentId,SyncKind.EDIT_SUB,job.displayName,job.text,base=job.text,dependency=job.id,batchId=job.batchId,revision=row.revision)}
                SyncKind.UPLOAD_PHOTO->{val row=requireNotNull(dao.photo(job.targetId));dao.updatePhoto(row.copy(remoteId=candidateId,remoteUrl=candidate.imageUri))}
                SyncKind.CREATE_COMMENT->{val row=requireNotNull(work.comment(job.targetId));work.putComment(row.copy(remoteId=candidateId));if(candidate.title!=job.text)enqueue(job.accountId,job.targetId,job.parentId,SyncKind.EDIT_COMMENT,job.displayName,job.text,base=job.text,dependency=job.id,revision=row.revision)}
                SyncKind.SET_COVER,SyncKind.SET_DESCRIPTION,SyncKind.EDIT_ALBUM_TITLE,SyncKind.SET_SUBTITLE,SyncKind.EDIT_SUB,SyncKind.DELETE_ALBUM,SyncKind.DELETE_SUB,SyncKind.DELETE_PHOTO,SyncKind.DO_LIKE,SyncKind.UNDO_LIKE,SyncKind.EDIT_COMMENT,SyncKind.DELETE_COMMENT->error("Not a creation")
            }
            work.updateJob(current.copy(status=SyncStatus.DONE.name,claimId=null));work.unpin(job.id)
        };scheduler.enqueue()
    }
    suspend fun keepMine(jobId:String,expected:AccountIdentity) {
        val(job,transport)=confirmation(jobId,expected);check(job.status==SyncStatus.CONFLICT.name)
        val fresh=transport.inspect(job);check(fresh==job.remoteText){"서버 값이 변경되었습니다. 다시 확인해주세요"};requireOwner(expected)
        db.withTransaction{val current=requireNotNull(work.job(jobId));check(current.status==job.status);work.updateJob(current.copy(status=SyncStatus.QUEUED.name,baseText=fresh,remoteText=null,claimId=null))};scheduler.enqueue()
    }
    suspend fun useServer(jobId:String,expected:AccountIdentity,observed:String) {
        val(job,transport)=confirmation(jobId,expected);val fresh=transport.inspect(job);check(fresh==observed){"서버 값이 변경되었습니다. 다시 확인해주세요"};requireOwner(expected)
        db.withTransaction{val current=requireNotNull(work.job(jobId));check(current.status==job.status);applyRemote(current,fresh);work.updateJob(current.copy(status=SyncStatus.DONE.name,claimId=null));work.unpin(jobId)
            work.jobs(job.accountId).filter{it.dependency==job.id && it.kind==job.kind && it.status==SyncStatus.QUEUED.name}.forEach{work.updateJob(it.copy(baseText=fresh))}
        };scheduler.enqueue();cleanupAfterCommit()
    }
    private suspend fun applyRemote(job:SyncJobEntity,value:String) {
        val family=if(job.kind in setOf("DO_LIKE","UNDO_LIKE"))setOf("DO_LIKE","UNDO_LIKE")else setOf(job.kind)
        if(work.jobs(job.accountId).any{it.targetId==job.targetId && it.id!=job.id && it.kind in family && it.revision>job.revision && it.status !in setOf("DONE","CANCELLED")})return
        when(SyncKind.valueOf(job.kind)) {
            SyncKind.EDIT_ALBUM_TITLE->dao.album(job.targetId)?.let{dao.updateAlbum(it.copy(title=value))}
            SyncKind.SET_SUBTITLE->dao.album(job.targetId)?.let{dao.updateAlbum(it.copy(subtitle=value))}
            SyncKind.SET_COVER->dao.album(job.targetId)?.let{dao.updateAlbum(it.copy(coverMediaName=null,remoteCoverUrl=value))}
            SyncKind.EDIT_SUB->dao.subAlbum(job.targetId)?.let{dao.updateSubAlbum(it.copy(title=value))}
            SyncKind.SET_DESCRIPTION->dao.photo(job.targetId)?.let{dao.updatePhoto(it.copy(description=value,descriptionKnown=true))}
            SyncKind.DO_LIKE,SyncKind.UNDO_LIKE->dao.photo(job.targetId)?.let{dao.updatePhoto(it.copy(isLiked=value=="true",likesKnown=true,likeCount=it.likeCount?.let{count->(count + if(it.isLiked==(value=="true"))0 else if(value=="true")1 else -1).coerceAtLeast(0)}))}
            SyncKind.EDIT_COMMENT->work.comment(job.targetId)?.let{work.putComment(it.copy(text=value))}
            SyncKind.DELETE_ALBUM->dao.album(job.targetId)?.let{if(it.revision==job.revision){if(value==job.text)dao.deleteAlbum(it.localId)else dao.updateAlbum(it.copy(deleted=false))}}
            SyncKind.DELETE_SUB->dao.subAlbum(job.targetId)?.let{if(it.revision==job.revision){if(value==job.text)dao.deleteSubAlbum(it.localId)else dao.updateSubAlbum(it.copy(deleted=false))}}
            SyncKind.DELETE_PHOTO->dao.photo(job.targetId)?.let{if(it.revision==job.revision){if(value==job.text)dao.deletePhoto(it.localId)else dao.updatePhoto(it.copy(deleted=false))}}
            SyncKind.DELETE_COMMENT->work.comment(job.targetId)?.let{if(it.revision==job.revision){if(value==job.text)work.deleteComment(it.localId)else work.putComment(it.copy(deleted=false))}}
            SyncKind.CREATE_ALBUM,SyncKind.CREATE_SUB,SyncKind.UPLOAD_PHOTO,SyncKind.CREATE_COMMENT->error("Link the created resource")
        }
    }
    suspend fun retryConfirmed(jobId:String,expected:AccountIdentity) {
        val(job,_)=confirmation(jobId,expected);check(job.status==SyncStatus.UNKNOWN.name)
        db.withTransaction{val current=requireNotNull(work.job(jobId));check(current.status==job.status);work.updateJob(current.copy(status=SyncStatus.QUEUED.name,claimId=null))};scheduler.enqueue()
    }

    suspend fun cancel(jobId:String,expected:AccountIdentity) {
        requireOwner(expected)
        db.withTransaction {
            val job=requireNotNull(work.job(jobId));check(job.accountId==expected.accountId)
            if(job.batchId!=null) {
                val batch=requireNotNull(work.batch(requireNotNull(job.batchId)));val jobs=work.jobs(expected.accountId).filter{it.batchId==batch.id}
                check(jobs.none{it.status in setOf(SyncStatus.SENDING.name,SyncStatus.UNKNOWN.name,SyncStatus.AWAITING_ID.name)}){"먼저 서버 반영 상태를 확인해주세요"}
                jobs.filter{it.status!=SyncStatus.DONE.name}.forEach{work.updateJob(it.copy(status=SyncStatus.CANCELLED.name,claimId=null));work.unpin(it.id)}
                batch.replicaIds.split(",").forEach{id->dao.album(id)?.let{check(it.libraryId==LibraryScope.Account(expected.accountId).id);dao.deleteAlbum(id)}}
            } else {
                check(job.status !in setOf(SyncStatus.SENDING.name,SyncStatus.UNKNOWN.name,SyncStatus.AWAITING_ID.name)){"먼저 서버 반영 상태를 확인해주세요"}
                if(SyncKind.valueOf(job.kind) in setOf(SyncKind.CREATE_ALBUM,SyncKind.CREATE_SUB,SyncKind.UPLOAD_PHOTO,SyncKind.CREATE_COMMENT)){check(cancelUnsentTree(job.targetId)){"먼저 서버 반영 상태를 확인해주세요"};when(SyncKind.valueOf(job.kind)){SyncKind.CREATE_ALBUM->dao.deleteAlbum(job.targetId);SyncKind.CREATE_SUB->dao.deleteSubAlbum(job.targetId);SyncKind.UPLOAD_PHOTO->dao.deletePhoto(job.targetId);SyncKind.CREATE_COMMENT->work.deleteComment(job.targetId);else->error("creation")};return@withTransaction}
                applyRemote(job,if(job.kind in setOf("DELETE_ALBUM","DELETE_SUB","DELETE_PHOTO","DELETE_COMMENT"))"서버에 아직 존재합니다"else job.baseText.orEmpty());work.updateJob(job.copy(status=SyncStatus.DONE.name,claimId=null));work.unpin(job.id)
                work.jobs(job.accountId).filter{it.dependency==job.id && it.kind==job.kind && it.status==SyncStatus.QUEUED.name}.forEach{work.updateJob(it.copy(baseText=job.baseText))}
            }
        };scheduler.enqueue();cleanupAfterCommit()
    }
    /** Cancel only a never-sent creation tree, in the caller's Room transaction. */
    internal suspend fun cancelUnsentTree(target:String):Boolean {
        val owner=currentOwner();val jobs=work.jobs(owner.accountId)
        val root=jobs.firstOrNull{it.targetId==target && it.kind in setOf("CREATE_ALBUM","CREATE_SUB","UPLOAD_PHOTO","CREATE_COMMENT") && it.status !in setOf("DONE","CANCELLED")} ?: return false
        if(root.batchId!=null)return false
        val ids=mutableSetOf(target)
        do{val size=ids.size;jobs.filter{it.parentId in ids}.forEach{ids+=it.targetId}}while(ids.size!=size)
        val tree=jobs.filter{it.targetId in ids && it.status!="CANCELLED"}
        check(tree.none{it.status in setOf("DONE","SENDING","UNKNOWN","AWAITING_ID")}){"서버 반영 여부를 먼저 확인해주세요"}
        tree.forEach{work.updateJob(it.copy(status="CANCELLED",claimId=null));work.unpin(it.id)}
        return true
    }
    private suspend fun cleanupAfterCommit(){cord.eoeo.momentwo.core.common.runSuspendCatching{media.cleanUnused()}.onFailure{android.util.Log.e("AccountSync","Committed; cleanup deferred",it)}}

}
