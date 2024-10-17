package cord.eoeo.momentwo.data.photo.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cord.eoeo.momentwo.data.photo.local.entity.PhotoRemoteKeyEntity

@Dao
interface PhotoRemoteKeyDao {
    @Query("SELECT * FROM photo_key WHERE id = (SELECT MAX(id) FROM photo_key WHERE album_id = :albumId AND sub_album_id = :subAlbumId)")
    suspend fun getLastKey(albumId: Int, subAlbumId: Int): PhotoRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: PhotoRemoteKeyEntity)

    @Query("DELETE FROM photo_key")
    suspend fun clearKeys()
}
