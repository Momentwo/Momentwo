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

class RoomLocalLibraryRepository @Inject constructor(private val db: MomentwoDatabase, @ApplicationContext private val context: Context, private val media: LocalMediaStore = LocalMediaStore(context, db), private val preferences: cord.eoeo.momentwo.core.datastore.PreferenceRepository? = null) : LocalLibraryRepository {
    private val dao get() = db.libraryDao()
    override suspend fun initialize() { dao.insertLibrary(LibraryEntity(LibraryScope.Device.id, null, "이 기기")) }
    private fun uri(name: String?): String = name?.let { media.file(it).toUri().toString() }.orEmpty()
    private fun LocalAlbumEntity.model() = LocalAlbum(localId, libraryId, title, subtitle, if (coverMediaName == null) remoteCoverUrl else uri(coverMediaName), remoteId, revision, role, subsLoaded)
    private fun LocalSubAlbumEntity.model() = LocalSubAlbum(localId, albumId, title, remoteId, revision, photosLoaded)
    private fun LocalPhotoEntity.model() = LocalPhoto(localId, subAlbumId, if (mediaName == null) remoteUrl else uri(mediaName), mediaName, remoteId, description, descriptionKnown, favorite, isLiked, likeCount, revision)
    override fun albums(scope: LibraryScope): Flow<List<LocalAlbum>> = when(scope) {
        LibraryScope.Device -> dao.albums(scope.id).map { rows -> rows.map { it.model() } }
        is LibraryScope.Account -> combine(dao.albums(scope.id), preferences?.identities() ?: flowOf(null)) { rows, identity -> if(identity?.accountId == scope.accountId) rows.map { it.model() } else emptyList() }
    }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun album(id: String): Flow<LocalAlbum?> = dao.observeAlbum(id).flatMapLatest { row ->
        if(row == null || row.libraryId == LibraryScope.Device.id) flowOf(row?.model())
        else (preferences?.identities() ?: flowOf(null)).map { if(row.libraryId == it?.accountId?.let { account -> LibraryScope.Account(account).id }) row.model() else null }
    }
    override fun subAlbums(albumId: String) = combine(album(albumId),dao.subAlbums(albumId)) { parent, rows -> if(parent != null) rows.map { it.model() } else emptyList() }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun subAlbum(id: String) = dao.observeSubAlbum(id).flatMapLatest { row -> if(row == null) flowOf(null) else album(row.albumId).map { parent -> if(parent != null) row.model() else null } }
    override fun photos(subAlbumId: String) = combine(subAlbum(subAlbumId),dao.photos(subAlbumId)) { parent,rows -> if(parent != null) rows.map { it.model() } else emptyList() }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun photo(id: String) = dao.observePhoto(id).flatMapLatest { row -> if(row == null) flowOf(null) else subAlbum(row.subAlbumId).map { parent -> if(parent != null) row.model() else null } }
    override fun memos(photoId: String) = combine(photo(photoId),dao.memos(photoId)) { parent,rows -> if(parent != null) rows.map { LocalMemo(it.localId,it.photoId,it.text,it.createdAt) } else emptyList() }
    override suspend fun createAlbum(title: String, localId: String): String = db.withTransaction {
        require(title.isNotBlank()) { "앨범 이름을 입력해주세요" }; initialize()
        val existing = dao.album(localId)
        check(existing == null || (existing.libraryId == LibraryScope.Device.id && existing.title == title.trim()))
        dao.insertAlbum(LocalAlbumEntity(localId, LibraryScope.Device.id, title.trim(), createdAt = System.currentTimeMillis()))
        localId
    }
    private suspend fun deviceAlbum(id: String): LocalAlbumEntity = requireNotNull(dao.album(id)) { "앨범을 찾을 수 없습니다" }.also { check(it.libraryId == LibraryScope.Device.id) { "다른 라이브러리의 자료입니다" } }
    override suspend fun createSubAlbum(albumId: String, title: String, localId: String): String = db.withTransaction {
        deviceAlbum(albumId); require(title.isNotBlank()) { "서브앨범 이름을 입력해주세요" }
        val existing = dao.subAlbum(localId)
        check(existing == null || (existing.albumId == albumId && existing.title == title.trim()))
        dao.insertSubAlbum(LocalSubAlbumEntity(localId, albumId, title.trim(), createdAt = System.currentTimeMillis()))
        localId
    }
    override suspend fun editAlbum(id: String, title: String, subtitle: String) = db.withTransaction {
        require(title.isNotBlank()) { "앨범 이름을 입력해주세요" }; val old = deviceAlbum(id)
        dao.updateAlbum(old.copy(title = title.trim(), subtitle = subtitle, revision = old.revision + 1))
    }
    override suspend fun editSubAlbum(id: String, title: String) = db.withTransaction {
        require(title.isNotBlank()) { "서브앨범 이름을 입력해주세요" }
        val old = requireNotNull(dao.subAlbum(id)); deviceAlbum(old.albumId)
        dao.updateSubAlbum(old.copy(title = title.trim(), revision = old.revision + 1))
    }
    override suspend fun setCover(id: String, source: android.net.Uri?) {
        if (source == null) db.withTransaction { val old = deviceAlbum(id); dao.updateAlbum(old.copy(coverMediaName = null, revision = old.revision + 1)) }
        else media.withImported(source) { name -> db.withTransaction { val old = deviceAlbum(id); dao.updateAlbum(old.copy(coverMediaName = name, revision = old.revision + 1)) } }
        cleanupAfterCommit()
    }
    private suspend fun deviceSub(id: String): LocalSubAlbumEntity = requireNotNull(dao.subAlbum(id)).also { deviceAlbum(it.albumId) }
    private suspend fun devicePhoto(id: String): LocalPhotoEntity = requireNotNull(dao.photo(id)).also { deviceSub(it.subAlbumId) }
    override suspend fun importPhoto(subAlbumId: String, source: android.net.Uri, localId: String): String {
        deviceSub(subAlbumId)
        // The same intent may be resumed after SQLite commits; it must not copy another file.
        dao.photo(localId)?.let { check(it.subAlbumId == subAlbumId); return localId }
        return media.withImported(source) { name -> db.withTransaction {
            deviceSub(subAlbumId)
            val existing = dao.photo(localId); check(existing == null || existing.subAlbumId == subAlbumId)
            dao.insertPhoto(LocalPhotoEntity(localId, subAlbumId, name, createdAt = System.currentTimeMillis()))
            localId
        } }
    }
    override suspend fun setDescription(id: String, text: String) { db.withTransaction { val old = devicePhoto(id); dao.updatePhoto(old.copy(description = text, descriptionKnown = true, revision = old.revision + 1)) } }
    override suspend fun setFavorite(id: String, value: Boolean) { db.withTransaction { val old = devicePhoto(id); dao.updatePhoto(old.copy(favorite = value, revision = old.revision + 1)) } }
    override suspend fun saveMemo(photoId: String, text: String, memoId: String): String = db.withTransaction {
        devicePhoto(photoId); require(text.isNotBlank())
        val old = dao.memo(memoId)
        if (old == null) dao.insertMemo(LocalMemoEntity(memoId, photoId, text, System.currentTimeMillis()))
        else { check(old.photoId == photoId); dao.updateMemo(old.copy(text = text)) }
        memoId
    }
    override suspend fun deleteMemo(id: String) { db.withTransaction { val old = requireNotNull(dao.memo(id)); devicePhoto(old.photoId); dao.deleteMemo(id) } }
    override suspend fun deletePhoto(id: String) { db.withTransaction { devicePhoto(id); dao.deletePhoto(id) }; cleanupAfterCommit() }
    override suspend fun exportPhoto(id: String) { val name = requireNotNull(devicePhoto(id).mediaName) { "사진 파일을 내려받은 뒤 내보낼 수 있습니다" }; media.export(name) }
    private suspend fun cleanupAfterCommit() { cord.eoeo.momentwo.core.common.runSuspendCatching { media.cleanUnused() }.onFailure { android.util.Log.e("LocalLibrary", "Saved; cleanup deferred", it) } }
    override suspend fun cleanUnusedMedia() { media.cleanUnused() }
    override suspend fun deleteAlbum(id: String) { db.withTransaction { deviceAlbum(id); dao.deleteAlbum(id) }; cleanupAfterCommit() }
    override suspend fun deleteSubAlbum(id: String) { db.withTransaction { val old = requireNotNull(dao.subAlbum(id)); deviceAlbum(old.albumId); dao.deleteSubAlbum(id) }; cleanupAfterCommit() }
}
