package cord.eoeo.momentwo.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_key")
data class PhotoRemoteKeyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "album_id") val albumId: Int,
    @ColumnInfo(name = "sub_album_id") val subAlbumId: Int,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long,
    @ColumnInfo(name = "next_cursor") val nextCursor: Int?,
)
