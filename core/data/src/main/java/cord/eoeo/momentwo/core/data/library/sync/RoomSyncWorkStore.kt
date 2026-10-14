package cord.eoeo.momentwo.core.data.library.sync

import androidx.room.withTransaction
import cord.eoeo.momentwo.core.database.*
import cord.eoeo.momentwo.core.model.*
import javax.inject.Inject

internal class RoomSyncWorkStore @Inject constructor(private val db: MomentwoDatabase) : SyncWorkStore {
    private val dao get() = db.syncDao()
    override suspend fun recover(accountId: String) = db.withTransaction {
        dao.jobs(accountId).filter { it.status in setOf(SyncStatus.PREPARING.name,SyncStatus.SENDING.name) }.forEach { job ->
            val safe = job.status == SyncStatus.PREPARING.name || (SyncKind.valueOf(job.kind) in setOf(SyncKind.UPLOAD_PHOTO,SyncKind.SET_COVER) && job.mediaName!=null && job.transferPhase < 2)
            dao.updateJob(job.copy(status = if(safe) SyncStatus.QUEUED.name else SyncStatus.UNKNOWN.name,claimId = null))
        }
    }
    override suspend fun next(accountId: String): SyncJobEntity? {
        val jobs = dao.jobs(accountId)
        return jobs.firstOrNull { it.status == SyncStatus.QUEUED.name && (it.dependency == null || jobs.any { dependency -> dependency.id == it.dependency && dependency.status == SyncStatus.DONE.name }) }
    }
    override suspend fun claim(job: SyncJobEntity, claim: String) = dao.transition(job.id,SyncStatus.QUEUED.name,SyncStatus.PREPARING.name,claim) == 1
    override suspend fun sending(job: SyncJobEntity, claim: String): Boolean = db.withTransaction {
        val current = dao.job(job.id) ?: return@withTransaction false
        if(current.claimId != claim || current.status != SyncStatus.PREPARING.name) return@withTransaction false
        val media = SyncKind.valueOf(current.kind) in setOf(SyncKind.UPLOAD_PHOTO,SyncKind.SET_COVER) && current.mediaName!=null
        dao.updateJob(current.copy(status = SyncStatus.SENDING.name,transferPhase = if(media) current.transferPhase else 2))
        true
    }
    override suspend fun settle(job: SyncJobEntity, claim: String, status: SyncStatus, remote: String?, extra: String?) = db.withTransaction {
        val current = dao.job(job.id) ?: return@withTransaction
        if(current.claimId != claim || current.status !in setOf(SyncStatus.PREPARING.name,SyncStatus.SENDING.name)) return@withTransaction
        dao.updateJob(current.copy(status = status.name,remoteText = remote,remoteExtra = extra,claimId = null))
        if(status == SyncStatus.DONE || status == SyncStatus.CANCELLED) dao.unpin(job.id)
        if(status==SyncStatus.DONE) when(SyncKind.valueOf(job.kind)) {
            SyncKind.DELETE_ALBUM -> db.libraryDao().album(job.targetId)?.let { if(it.deleted && it.revision==job.revision)db.libraryDao().deleteAlbum(it.localId) }
            SyncKind.DELETE_SUB -> db.libraryDao().subAlbum(job.targetId)?.let { if(it.deleted && it.revision==job.revision)db.libraryDao().deleteSubAlbum(it.localId) }
            SyncKind.DELETE_PHOTO -> db.libraryDao().photo(job.targetId)?.let { if(it.deleted && it.revision==job.revision)db.libraryDao().deletePhoto(it.localId) }
            SyncKind.DELETE_COMMENT -> dao.comment(job.targetId)?.let { if(it.deleted && it.revision==job.revision)dao.deleteComment(it.localId) }
            SyncKind.CREATE_ALBUM,SyncKind.SET_COVER,SyncKind.CREATE_SUB,SyncKind.UPLOAD_PHOTO,SyncKind.SET_DESCRIPTION,SyncKind.EDIT_ALBUM_TITLE,SyncKind.SET_SUBTITLE,SyncKind.EDIT_SUB,SyncKind.DO_LIKE,SyncKind.UNDO_LIKE,SyncKind.CREATE_COMMENT,SyncKind.EDIT_COMMENT -> Unit
        }
    }
    override suspend fun current(id: String) = dao.job(id)
    override suspend fun interrupted(job: SyncJobEntity, claim: String) = db.withTransaction {
        val current = dao.job(job.id) ?: return@withTransaction
        if(current.claimId != claim) return@withTransaction
        val safe = current.status == SyncStatus.PREPARING.name || (SyncKind.valueOf(current.kind) in setOf(SyncKind.UPLOAD_PHOTO,SyncKind.SET_COVER) && current.mediaName!=null && current.transferPhase < 2)
        if(current.status in setOf(SyncStatus.PREPARING.name,SyncStatus.SENDING.name)) dao.updateJob(current.copy(status = if(safe) SyncStatus.QUEUED.name else SyncStatus.UNKNOWN.name,claimId = null))
    }
}
