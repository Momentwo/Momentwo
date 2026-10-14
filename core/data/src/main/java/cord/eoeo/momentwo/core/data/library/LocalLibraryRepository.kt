package cord.eoeo.momentwo.core.data.library

import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow

interface LocalLibraryRepository {
    suspend fun initialize()
    fun albums(scope: LibraryScope): Flow<List<LocalAlbum>>
    fun album(id: String): Flow<LocalAlbum?>
    fun subAlbums(albumId: String): Flow<List<LocalSubAlbum>>
    fun subAlbum(id: String): Flow<LocalSubAlbum?>
    fun photos(subAlbumId: String): Flow<List<LocalPhoto>>
    fun photo(id: String): Flow<LocalPhoto?>
    fun memos(photoId: String): Flow<List<LocalMemo>>
    suspend fun createAlbum(title: String, localId: String = java.util.UUID.randomUUID().toString()): String
    suspend fun createSubAlbum(albumId: String, title: String, localId: String = java.util.UUID.randomUUID().toString()): String
    suspend fun editAlbum(id: String, title: String, subtitle: String)
    suspend fun editAlbumTitle(id: String, title: String)
    suspend fun editAlbumSubtitle(id: String, subtitle: String)
    suspend fun deleteSubAlbums(albumId: String, ids: Set<String>)
    suspend fun deletePhotos(subAlbumId: String, ids: Set<String>)
    suspend fun editSubAlbum(id: String, title: String)
    suspend fun setCover(id: String, source: android.net.Uri?)
    suspend fun importPhoto(subAlbumId: String, source: android.net.Uri, localId: String = java.util.UUID.randomUUID().toString()): String
    suspend fun setDescription(id: String, text: String)
    suspend fun setFavorite(id: String, value: Boolean)
    suspend fun saveMemo(photoId: String, text: String, memoId: String = java.util.UUID.randomUUID().toString()): String
    suspend fun deleteMemo(id: String)
    suspend fun deletePhoto(id: String)
    suspend fun exportPhoto(id: String)
    suspend fun cleanUnusedMedia()
    suspend fun deleteAlbum(id: String)
    suspend fun deleteSubAlbum(id: String)
}
