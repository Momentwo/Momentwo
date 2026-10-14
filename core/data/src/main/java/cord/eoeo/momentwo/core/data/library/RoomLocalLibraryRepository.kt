package cord.eoeo.momentwo.core.data.library

import android.content.Context
import androidx.core.net.toUri
import androidx.room.withTransaction
import cord.eoeo.momentwo.core.database.*
import cord.eoeo.momentwo.core.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.*

class RoomLocalLibraryRepository @Inject constructor(private val db: MomentwoDatabase, @ApplicationContext private val context: Context, private val media: LocalMediaStore = LocalMediaStore(context, db), private val preferences: cord.eoeo.momentwo.core.datastore.PreferenceRepository? = null, private val sync: AccountSyncRepository? = null) : LocalLibraryRepository {
    private val dao get() = db.libraryDao()
    override suspend fun initialize() { dao.insertLibrary(LibraryEntity(LibraryScope.Device.id, null, "이 기기")) }
    private fun uri(name: String?): String = name?.let { media.file(it).toUri().toString() }.orEmpty()
    private fun LocalAlbumEntity.model() = LocalAlbum(localId, libraryId, title, subtitle, if (coverMediaName == null) remoteCoverUrl else uri(coverMediaName), remoteId, revision, role, subsLoaded, locallyCreated)
    private fun LocalSubAlbumEntity.model() = LocalSubAlbum(localId, albumId, title, remoteId, revision, photosLoaded)
    private fun LocalPhotoEntity.model(libraryId: String = "device", role: String? = null,copying:Boolean=false) = LocalPhoto(localId, subAlbumId, if(mediaName!=null)uri(mediaName)else cacheName?.takeIf { File(context.cacheDir,"server-media/$it").isFile }?.let { File(context.cacheDir,"server-media/$it").toUri().toString() } ?: remoteUrl, mediaName, remoteId, description, descriptionKnown, favorite, isLiked, likeCount, revision, libraryId, role, likesKnown,commentsLoaded,copying)
    override fun albums(scope: LibraryScope): Flow<List<LocalAlbum>> = when(scope) {
        LibraryScope.Device -> dao.albums(scope.id).map { rows -> rows.map { it.model() } }
        is LibraryScope.Account -> combine(dao.albums(scope.id), preferences?.identities() ?: flowOf(null),db.syncDao().observeJobs(scope.accountId)) { rows, identity,jobs -> if(identity?.accountId == scope.accountId) rows.map { row -> row.model().copy(copying=jobs.any{it.batchId!=null && db.syncDao().batch(requireNotNull(it.batchId))?.replicaIds?.split(",")?.contains(row.localId)==true && it.status !in setOf("DONE","CANCELLED")}) } else emptyList() }
    }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun album(id: String): Flow<LocalAlbum?> = combine(dao.observeAlbum(id),db.syncDao().copyJobs(id)) { row,jobs -> row to jobs.any { it.status !in setOf(SyncStatus.DONE.name,SyncStatus.CANCELLED.name) } }.flatMapLatest { (row,copying) ->
        if(row == null || row.libraryId == LibraryScope.Device.id) flowOf(row?.model()?.copy(copying=copying))
        else (preferences?.identities() ?: flowOf(null)).map { if(row.libraryId == it?.accountId?.let { account -> LibraryScope.Account(account).id }) row.model().copy(copying=copying) else null }
    }
    override fun subAlbums(albumId: String) = combine(album(albumId),dao.subAlbums(albumId)) { parent, rows -> if(parent != null) rows.map { it.model() } else emptyList() }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun subAlbum(id: String) = dao.observeSubAlbum(id).flatMapLatest { row -> if(row == null) flowOf(null) else album(row.albumId).map { parent -> if(parent != null) row.model() else null } }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun photos(subAlbumId: String): Flow<List<LocalPhoto>> = subAlbum(subAlbumId).flatMapLatest { sub -> if(sub == null) flowOf(emptyList()) else combine(album(sub.albumId),dao.photos(subAlbumId)) { parent,rows -> if(parent != null) rows.map { it.model(parent.libraryId,parent.role,parent.copying) } else emptyList() } }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun photo(id: String) = dao.observePhoto(id).flatMapLatest { row -> if(row == null) flowOf(null) else subAlbum(row.subAlbumId).flatMapLatest { sub -> if(sub == null) flowOf(null) else album(sub.albumId).map { parent -> if(parent != null) row.model(parent.libraryId,parent.role,parent.copying) else null } } }
    override fun memos(photoId: String) = combine(photo(photoId),dao.memos(photoId)) { parent,rows -> if(parent != null) rows.map { LocalMemo(it.localId,it.photoId,it.text,it.createdAt) } else emptyList() }
    override suspend fun createAlbum(title: String, localId: String): String = db.withTransaction {
        require(title.isNotBlank()) { "앨범 이름을 입력해주세요" }; initialize()
        val existing = dao.album(localId)
        check(existing == null || (existing.libraryId == LibraryScope.Device.id && existing.title == title.trim()))
        dao.insertAlbum(LocalAlbumEntity(localId, LibraryScope.Device.id, title.trim(), createdAt = System.currentTimeMillis()))
        localId
    }
    private suspend fun writableAlbum(id: String,kind:SyncKind?=null):LocalAlbumEntity = requireNotNull(dao.album(id)){"앨범을 찾을 수 없습니다"}.also {check(!it.deleted);if(it.libraryId!=LibraryScope.Device.id)checkNotNull(sync){"다른 라이브러리의 자료입니다"}.ownerFor(it,kind)}
    private suspend fun record(album:LocalAlbumEntity,target:String,parent:String?,kind:SyncKind,value:String,base:String?,revision:Long,mediaName:String?=null){if(album.libraryId!=LibraryScope.Device.id){val queue=requireNotNull(sync);val owner=queue.ownerFor(album,kind);queue.mutation(owner,target,parent,kind,album.title,value,base,revision,mediaName)}}
    override suspend fun createSubAlbum(albumId: String, title: String, localId: String): String = db.withTransaction {
        val album=writableAlbum(albumId,SyncKind.CREATE_SUB); require(title.isNotBlank()) { "서브앨범 이름을 입력해주세요" }
        val existing = dao.subAlbum(localId)
        check(existing == null || (existing.albumId == albumId && existing.title == title.trim()))
        dao.insertSubAlbum(LocalSubAlbumEntity(localId, albumId, title.trim(), createdAt = System.currentTimeMillis()))
        if(existing==null && album.libraryId!="device"){val owner=requireNotNull(sync).currentOwner();db.syncDao().putLink(SyncLinkEntity(localId,owner.accountId,"SUB",albumId,null));record(album,localId,albumId,SyncKind.CREATE_SUB,title.trim(),null,0)}
        localId
    }
    override suspend fun editAlbum(id: String, title: String, subtitle: String) = db.withTransaction {
        require(title.isNotBlank()) { "앨범 이름을 입력해주세요" }; val old = writableAlbum(id,SyncKind.EDIT_ALBUM_TITLE)
        val next=old.copy(title=title.trim(),subtitle=subtitle,revision=old.revision+1);dao.updateAlbum(next)
        if(old.title!=next.title)record(old,id,null,SyncKind.EDIT_ALBUM_TITLE,next.title,old.title,next.revision)
        if(old.subtitle!=subtitle)record(old,id,null,SyncKind.SET_SUBTITLE,subtitle,old.subtitle,next.revision)
    }
    override suspend fun editSubAlbum(id: String, title: String) = db.withTransaction {
        require(title.isNotBlank()) { "서브앨범 이름을 입력해주세요" }
        val old = requireNotNull(dao.subAlbum(id));val album=writableAlbum(old.albumId,SyncKind.EDIT_SUB);check(!old.deleted)
        val next=old.copy(title=title.trim(),revision=old.revision+1);dao.updateSubAlbum(next)
        record(album,id,old.albumId,SyncKind.EDIT_SUB,next.title,old.title,next.revision)
    }
    override suspend fun setCover(id: String, source: android.net.Uri?) {
        if (source == null) db.withTransaction { val old=writableAlbum(id,SyncKind.SET_COVER);dao.updateAlbum(old.copy(coverMediaName=null,remoteCoverUrl="",revision=old.revision+1));record(old,id,null,SyncKind.SET_COVER,"",old.remoteCoverUrl,old.revision+1) }
        else media.withImported(source) { name -> db.withTransaction { val old=writableAlbum(id,SyncKind.SET_COVER);dao.updateAlbum(old.copy(coverMediaName=name,revision=old.revision+1));record(old,id,null,SyncKind.SET_COVER,"",old.remoteCoverUrl,old.revision+1,name) } }
        cleanupAfterCommit()
    }
    private suspend fun writableSub(id:String,kind:SyncKind?=null):LocalSubAlbumEntity = requireNotNull(dao.subAlbum(id)).also {check(!it.deleted);writableAlbum(it.albumId,kind)}
    private suspend fun writablePhoto(id:String,kind:SyncKind?=null):LocalPhotoEntity = requireNotNull(dao.photo(id)).also{check(!it.deleted);writableSub(it.subAlbumId,kind)}
    override suspend fun importPhoto(subAlbumId: String, source: android.net.Uri, localId: String): String {
        writableSub(subAlbumId,SyncKind.UPLOAD_PHOTO)
        // The same intent may be resumed after SQLite commits; it must not copy another file.
        dao.photo(localId)?.let { check(it.subAlbumId == subAlbumId); return localId }
        return media.withImported(source) { name -> db.withTransaction {
            writableSub(subAlbumId,SyncKind.UPLOAD_PHOTO)
            val existing = dao.photo(localId); check(existing == null || existing.subAlbumId == subAlbumId)
            dao.insertPhoto(LocalPhotoEntity(localId, subAlbumId, name, createdAt = System.currentTimeMillis()))
            val sub=requireNotNull(dao.subAlbum(subAlbumId));val album=writableAlbum(sub.albumId,SyncKind.UPLOAD_PHOTO)
            if(existing==null && album.libraryId!="device"){val owner=requireNotNull(sync).currentOwner();db.syncDao().putLink(SyncLinkEntity(localId,owner.accountId,"PHOTO",subAlbumId,null));record(album,localId,subAlbumId,SyncKind.UPLOAD_PHOTO,"",null,0,name)}
            localId
        } }
    }
    override suspend fun setDescription(id: String, text: String) { db.withTransaction { val old=writablePhoto(id,SyncKind.SET_DESCRIPTION);val sub=requireNotNull(dao.subAlbum(old.subAlbumId));val album=writableAlbum(sub.albumId,SyncKind.SET_DESCRIPTION)
        val next=old.copy(description=text,descriptionKnown=true,revision=old.revision+1);dao.updatePhoto(next);record(album,id,old.subAlbumId,SyncKind.SET_DESCRIPTION,text,if(old.descriptionKnown)old.description else null,next.revision) } }
    override suspend fun setFavorite(id: String, value: Boolean) { db.withTransaction { val old = writablePhoto(id); dao.updatePhoto(old.copy(favorite = value, revision = old.revision + 1)) } }
    override suspend fun saveMemo(photoId: String, text: String, memoId: String): String = db.withTransaction {
        writablePhoto(photoId); require(text.isNotBlank())
        val old = dao.memo(memoId)
        if (old == null) dao.insertMemo(LocalMemoEntity(memoId, photoId, text, System.currentTimeMillis()))
        else { check(old.photoId == photoId); dao.updateMemo(old.copy(text = text)) }
        memoId
    }
    override suspend fun deleteMemo(id: String) { db.withTransaction { val old = requireNotNull(dao.memo(id)); writablePhoto(old.photoId); dao.deleteMemo(id) } }
    override suspend fun deletePhoto(id: String) { db.withTransaction {val photo=writablePhoto(id,SyncKind.DELETE_PHOTO);val sub=requireNotNull(dao.subAlbum(photo.subAlbumId));val album=writableAlbum(sub.albumId,SyncKind.DELETE_PHOTO);if(album.libraryId=="device")dao.deletePhoto(id)else if(requireNotNull(sync).cancelUnsentTree(id)){dao.deletePhoto(id)}else{val next=photo.copy(deleted=true,revision=photo.revision+1);dao.updatePhoto(next);record(album,id,photo.subAlbumId,SyncKind.DELETE_PHOTO,"",null,next.revision,photo.mediaName)}}; cleanupAfterCommit() }
    override suspend fun exportPhoto(id: String) { val photo=writablePhoto(id);if(photo.mediaName!=null)media.export(requireNotNull(photo.mediaName))else{val name=requireNotNull(photo.cacheName){"사진 파일을 내려받은 뒤 내보낼 수 있습니다"};check(Regex("[0-9a-f-]+\\.img").matches(name));media.exportFile(File(context.cacheDir,"server-media/$name"),"Momentwo-${photo.localId}.jpg")} }
    private suspend fun cleanupAfterCommit() { cord.eoeo.momentwo.core.common.runSuspendCatching { media.cleanUnused() }.onFailure { android.util.Log.e("LocalLibrary", "Saved; cleanup deferred", it) } }
    override suspend fun cleanUnusedMedia() { media.cleanUnused() }
    override suspend fun deleteAlbum(id: String) { db.withTransaction {val album=writableAlbum(id,SyncKind.DELETE_ALBUM);if(album.libraryId=="device")dao.deleteAlbum(id)else if(requireNotNull(sync).cancelUnsentTree(id)){dao.deleteAlbum(id)}else{val next=album.copy(deleted=true,revision=album.revision+1);dao.updateAlbum(next);val owner=requireNotNull(sync).currentOwner();requireNotNull(sync).mutation(owner,id,null,SyncKind.DELETE_ALBUM,album.title,"",null,next.revision)}}; cleanupAfterCommit() }
    override suspend fun deleteSubAlbum(id: String) { db.withTransaction {val sub=writableSub(id,SyncKind.DELETE_SUB);val album=writableAlbum(sub.albumId,SyncKind.DELETE_SUB);if(album.libraryId=="device")dao.deleteSubAlbum(id)else if(requireNotNull(sync).cancelUnsentTree(id)){dao.deleteSubAlbum(id)}else{val next=sub.copy(deleted=true,revision=sub.revision+1);dao.updateSubAlbum(next);val owner=requireNotNull(sync).currentOwner();requireNotNull(sync).mutation(owner,id,sub.albumId,SyncKind.DELETE_SUB,album.title,"",null,next.revision)}}; cleanupAfterCommit() }
}
