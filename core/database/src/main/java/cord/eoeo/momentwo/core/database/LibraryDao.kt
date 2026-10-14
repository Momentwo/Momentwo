package cord.eoeo.momentwo.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library WHERE id = :id") suspend fun library(id: String): LibraryEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertLibrary(entity: LibraryEntity)
    @Query("SELECT * FROM local_album WHERE libraryId = :libraryId ORDER BY createdAt DESC, localId") fun albums(libraryId: String): Flow<List<LocalAlbumEntity>>
    @Query("SELECT * FROM local_album WHERE localId = :id") fun observeAlbum(id: String): Flow<LocalAlbumEntity?>
    @Query("SELECT * FROM local_album WHERE localId = :id") suspend fun album(id: String): LocalAlbumEntity?
    @Query("SELECT * FROM local_subalbum WHERE albumId = :albumId ORDER BY createdAt, localId") fun subAlbums(albumId: String): Flow<List<LocalSubAlbumEntity>>
    @Query("SELECT * FROM local_subalbum WHERE localId = :id") fun observeSubAlbum(id: String): Flow<LocalSubAlbumEntity?>
    @Query("SELECT * FROM local_subalbum WHERE localId = :id") suspend fun subAlbum(id: String): LocalSubAlbumEntity?
    @Query("SELECT * FROM local_photo WHERE subAlbumId = :subAlbumId ORDER BY createdAt, localId") fun photos(subAlbumId: String): Flow<List<LocalPhotoEntity>>
    @Query("SELECT * FROM local_photo WHERE localId = :id") fun observePhoto(id: String): Flow<LocalPhotoEntity?>
    @Query("SELECT * FROM local_photo WHERE localId = :id") suspend fun photo(id: String): LocalPhotoEntity?
    @Query("SELECT * FROM local_memo WHERE photoId = :photoId ORDER BY createdAt, localId") fun memos(photoId: String): Flow<List<LocalMemoEntity>>
    @Query("SELECT * FROM local_memo WHERE localId = :id") suspend fun memo(id: String): LocalMemoEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAlbum(entity: LocalAlbumEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertSubAlbum(entity: LocalSubAlbumEntity)
    @Insert suspend fun insertPhoto(entity: LocalPhotoEntity)
    @Insert suspend fun insertMemo(entity: LocalMemoEntity)
    @Update suspend fun updateAlbum(entity: LocalAlbumEntity)
    @Update suspend fun updateSubAlbum(entity: LocalSubAlbumEntity)
    @Update suspend fun updatePhoto(entity: LocalPhotoEntity)
    @Update suspend fun updateMemo(entity: LocalMemoEntity)
    @Query("DELETE FROM local_album WHERE localId = :id") suspend fun deleteAlbum(id: String)
    @Query("DELETE FROM local_subalbum WHERE localId = :id") suspend fun deleteSubAlbum(id: String)
    @Query("DELETE FROM local_photo WHERE localId = :id") suspend fun deletePhoto(id: String)
    @Query("DELETE FROM local_memo WHERE localId = :id") suspend fun deleteMemo(id: String)
    @Query("SELECT mediaName FROM local_photo WHERE mediaName IS NOT NULL UNION SELECT coverMediaName FROM local_album WHERE coverMediaName IS NOT NULL") suspend fun mediaReferences(): List<String>
}
