package cord.eoeo.momentwo.core.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cord.eoeo.momentwo.core.database.PhotoEntity

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo WHERE album_id = :albumId AND sub_album_id = :subAlbumId ORDER BY id")
    fun getPhotoPagingSource(albumId: Int, subAlbumId: Int): PagingSource<Int, PhotoEntity>

    @Query("SELECT id FROM photo ORDER BY id DESC LIMIT 1")
    suspend fun getLastPhotoId(): Int?

    @Query("UPDATE photo SET is_liked = :isLiked WHERE id = :photoId")
    suspend fun updateIsLiked(photoId: Int, isLiked: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg photos: PhotoEntity)

    @Query("DELETE FROM photo WHERE id IN (:photoIds)")
    suspend fun deleteByIds(photoIds: List<Int>)

    @Query("DELETE FROM photo WHERE id BETWEEN :start AND :end AND id NOT IN (:photoIds)")
    suspend fun deleteByRange(photoIds: List<Int>, start: Int, end: Int)
}
