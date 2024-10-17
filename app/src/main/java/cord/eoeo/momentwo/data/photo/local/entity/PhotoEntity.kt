package cord.eoeo.momentwo.data.photo.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cord.eoeo.momentwo.ui.model.ImageItem

@Entity(tableName = "photo")
data class PhotoEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "album_id") val albumId: Int,
    @ColumnInfo(name = "sub_album_id") val subAlbumId: Int,
    @ColumnInfo(name = "image_url") val imageUrl: String,
) {
    fun mapToImageItem(): ImageItem =
        ImageItem(
            id = id,
            imageUrl = imageUrl,
        )
}
