package cord.eoeo.momentwo.core.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cord.eoeo.momentwo.core.database.PhotoEntity

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo WHERE ownerId = :ownerId AND album_id = :albumId AND sub_album_id = :subAlbumId ORDER BY id")
    fun getPhotoPagingSource(albumId: Int, subAlbumId: Int, ownerId: String = ""): PagingSource<Int, PhotoEntity>

    @Query("SELECT id FROM photo WHERE ownerId = :ownerId AND album_id = :albumId AND sub_album_id = :subAlbumId ORDER BY id DESC LIMIT 1")
    suspend fun getLastPhotoId(albumId: Int, subAlbumId: Int, ownerId: String = ""): Int?

    @Query("UPDATE photo SET is_liked = :isLiked WHERE ownerId = :ownerId AND id = :photoId")
    suspend fun updateIsLiked(photoId: Int, isLiked: Boolean, ownerId: String = "")

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg photos: PhotoEntity)

    @Query("DELETE FROM photo WHERE ownerId = :ownerId AND id IN (:photoIds)")
    suspend fun deleteByIds(photoIds: List<Int>, ownerId: String = "")

    @Query("DELETE FROM photo WHERE ownerId = :ownerId AND album_id = :albumId AND sub_album_id = :subAlbumId AND id BETWEEN :start AND :end AND id NOT IN (:photoIds)")
    suspend fun deleteByRange(photoIds: List<Int>, start: Int, end: Int, albumId: Int, subAlbumId: Int, ownerId: String = "")
}
