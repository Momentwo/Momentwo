package cord.eoeo.momentwo.data.photo.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cord.eoeo.momentwo.data.photo.local.entity.PhotoEntity

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo WHERE album_id = :albumId AND sub_album_id = :subAlbumId ORDER BY id")
    fun getPhotoPagingSource(albumId: Int, subAlbumId: Int): PagingSource<Int, PhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg photos: PhotoEntity)

    @Delete
    suspend fun deleteAll(vararg photo: PhotoEntity)
}
