package cord.eoeo.momentwo.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cord.eoeo.momentwo.core.database.PhotoRemoteKeyEntity

@Dao
interface PhotoRemoteKeyDao {
    @Query("SELECT * FROM photo_key WHERE ownerId = :ownerId AND id = (SELECT MAX(id) FROM photo_key WHERE ownerId = :ownerId AND album_id = :albumId AND sub_album_id = :subAlbumId)")
    suspend fun getLastKey(albumId: Int, subAlbumId: Int, ownerId: String = ""): PhotoRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: PhotoRemoteKeyEntity)

    @Query("DELETE FROM photo_key WHERE ownerId = :ownerId AND album_id = :albumId AND sub_album_id = :subAlbumId")
    suspend fun clearKeys(albumId: Int, subAlbumId: Int, ownerId: String = "")
}
