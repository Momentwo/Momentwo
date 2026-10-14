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
class AccountLibraryRepository @Inject constructor(private val db:MomentwoDatabase,private val preferences:PreferenceRepository,private val clients:AccountClientFactory,@ApplicationContext private val context:Context) {
    fun identities()=preferences.identities()
    suspend fun logout()=preferences.clearSession()
    suspend fun storage():Pair<Long,Long> = withContext(Dispatchers.IO) { File(context.filesDir,"library-media").listFiles().orEmpty().sumOf {it.length()} to StatFs(context.filesDir.path).availableBytes }
    suspend fun refresh() {
        val session=clients.snapshot();clients.check(session)
        val albums=clients.client(session).create(AlbumService::class.java).getAlbumList().albumList
        clients.check(session)
        db.withTransaction {
            val libraryId=LibraryScope.Account(session.identity.accountId).id
            db.libraryDao().insertLibrary(LibraryEntity(libraryId,session.identity.accountId,session.identity.accountId))
            albums.forEach { album ->
                val old=db.libraryDao().remoteAlbum(libraryId,album.id)
                if(old == null) db.libraryDao().insertAlbum(LocalAlbumEntity(UUID.randomUUID().toString(),libraryId,album.title,album.subTitle,remoteId=album.id,createdAt=System.currentTimeMillis(),remoteCoverUrl=album.profileImage,subsLoaded=false))
                else db.libraryDao().updateAlbum(old.copy(title=album.title,subtitle=album.subTitle,remoteCoverUrl=album.profileImage))
            }
        }
    }
    suspend fun refreshAlbum(id: String) {
        val album=requireNotNull(db.libraryDao().album(id)); if(album.libraryId==LibraryScope.Device.id) return
        val session=clients.snapshot();check(album.libraryId==LibraryScope.Account(session.identity.accountId).id);clients.check(session)
        val remoteId=requireNotNull(album.remoteId)
        val transport=clients.client(session)
        val role=transport.create(AlbumService::class.java).getAlbumRole(remoteId).rules
        val subs=transport.create(cord.eoeo.momentwo.core.data.subalbum.remote.SubAlbumService::class.java).getSubAlbumList(remoteId).subAlbumList
        clients.check(session)
        db.withTransaction {
            subs.forEach { sub ->
                val old=db.libraryDao().remoteSub(id,sub.id)
                if(old==null) db.libraryDao().insertSubAlbum(LocalSubAlbumEntity(UUID.randomUUID().toString(),id,sub.title,remoteId=sub.id,createdAt=System.currentTimeMillis(),photosLoaded=false))
                else db.libraryDao().updateSubAlbum(old.copy(title=sub.title))
            }
            val latest=requireNotNull(db.libraryDao().album(id));db.libraryDao().updateAlbum(latest.copy(role=role,subsLoaded=true))
        }
    }

}
