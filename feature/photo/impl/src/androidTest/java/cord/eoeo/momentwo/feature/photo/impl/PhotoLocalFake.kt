package cord.eoeo.momentwo.feature.photo.impl
import cord.eoeo.momentwo.core.data.library.LocalLibraryRepository
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.*
internal class PhotoLocalFake : LocalLibraryRepository {
    var create: suspend (String, String) -> String = { _, id -> id }
    val created = mutableListOf<String>()
    var starts = 0
    var observed: LibraryScope? = null
    var initialize: suspend () -> Unit = {}
    val rows = MutableStateFlow<List<LocalAlbum>>(emptyList())
    override suspend fun initialize() { starts++; initialize.invoke() }
    override fun albums(scope: LibraryScope): Flow<List<LocalAlbum>> { observed = scope; return rows }
    val album = MutableStateFlow<LocalAlbum?>(LocalAlbum("album", "device", "old", "subtitle", "", null, 0))
    val subs = MutableStateFlow(listOf(LocalSubAlbum("child", "album", "child", null, 0)))
    var edit: suspend (String, String, String) -> Unit = { _, _, _ -> }
    var edits = 0
    override fun album(id: String): Flow<LocalAlbum?> = album
    override fun subAlbums(albumId: String): Flow<List<LocalSubAlbum>> = subs
    override fun subAlbum(id: String): Flow<LocalSubAlbum?> = flowOf(LocalSubAlbum("child","album","child",null,0,photosLoaded=false))
    override fun photos(subAlbumId: String): Flow<List<LocalPhoto>> = flowOf(emptyList())
    val photo = MutableStateFlow<LocalPhoto?>(LocalPhoto("photo","child","file:///photo.png","photo.png",null,"old",true,false,false,null,0))
    val notes = MutableStateFlow<List<LocalMemo>>(emptyList())
    var description: suspend (String) -> Unit = {}
    override fun photo(id: String): Flow<LocalPhoto?> = photo
    override fun memos(photoId: String): Flow<List<LocalMemo>> = notes
    override suspend fun createAlbum(title: String, localId: String): String { created += title; return create(title, localId) }
    override suspend fun createSubAlbum(albumId: String, title: String, localId: String): String = error("unused")
    override suspend fun editAlbum(id: String, title: String, subtitle: String) { edits++; edit(id,title,subtitle) }
    override suspend fun editAlbumTitle(id: String, title: String): Unit = error("unused")
    override suspend fun editAlbumSubtitle(id: String, subtitle: String): Unit = error("unused")
    override suspend fun deleteSubAlbums(albumId: String, ids: Set<String>): Unit = error("unused")
    override suspend fun deletePhotos(subAlbumId: String, ids: Set<String>): Unit = error("unused")
    override suspend fun editSubAlbum(id: String, title: String): Unit = error("unused")
    override suspend fun setCover(id: String, source: android.net.Uri?): Unit = error("unused")
    override suspend fun importPhoto(subAlbumId: String, source: android.net.Uri, localId: String): String = error("unused")
    override suspend fun setDescription(id: String, text: String) { description(text) }
    override suspend fun setFavorite(id: String, value: Boolean) { photo.value = photo.value!!.copy(favorite=value) }
    override suspend fun saveMemo(photoId: String, text: String, memoId: String): String { notes.value = notes.value + LocalMemo(memoId,photoId,text,0); return memoId }
    override suspend fun deleteMemo(id: String): Unit = error("unused")
    override suspend fun deletePhoto(id: String): Unit = error("unused")
    var export: suspend () -> Unit = {}
    override suspend fun exportPhoto(id: String) { export() }
    override suspend fun cleanUnusedMedia() {}
    override suspend fun deleteAlbum(id: String): Unit = error("unused")
    override suspend fun deleteSubAlbum(id: String): Unit = error("unused")
}
