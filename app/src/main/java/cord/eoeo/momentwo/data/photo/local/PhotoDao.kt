package cord.eoeo.momentwo.data.photo.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo WHERE album_id = :albumId AND sub_album_id = :subAlbumId")
    fun getPhotoList(albumId: Int, subAlbumId: Int): List<PhotoEntity>

    @Query("SELECT * FROM photo WHERE id = :photoId LIMIT 1")
    fun findById(photoId: Int): PhotoEntity

    @Insert
    fun insertALl(vararg photos: PhotoEntity)

    @Delete
    fun delete(photo: PhotoEntity)
}
