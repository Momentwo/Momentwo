package cord.eoeo.momentwo.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cord.eoeo.momentwo.core.model.PhotoItem

@Entity(tableName = "photo", primaryKeys = ["ownerId", "id"])
data class PhotoEntity(
    val id: Int,
    @ColumnInfo(name = "album_id") val albumId: Int,
    @ColumnInfo(name = "sub_album_id") val subAlbumId: Int,
    @ColumnInfo(name = "photo_url") val photoUrl: String,
    @ColumnInfo(name = "is_liked") val isLiked: Boolean,
    @ColumnInfo(defaultValue = "''") val ownerId: String = "",
) {
    fun mapToPhotoItem(): PhotoItem =
        PhotoItem(
            id = id,
            photoUrl = photoUrl,
            isLiked = isLiked,
        )
}
