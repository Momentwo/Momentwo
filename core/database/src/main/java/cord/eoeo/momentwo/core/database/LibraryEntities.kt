package cord.eoeo.momentwo.core.database

import androidx.room.*

@Entity(tableName = "library")
data class LibraryEntity(@PrimaryKey val id: String, val accountId: String?, val name: String)

@Entity(tableName = "local_album", foreignKeys = [ForeignKey(entity = LibraryEntity::class, parentColumns = ["id"], childColumns = ["libraryId"], onDelete = ForeignKey.CASCADE)], indices = [Index("libraryId")])
data class LocalAlbumEntity(@PrimaryKey val localId: String, val libraryId: String, val title: String, val subtitle: String = "", val coverMediaName: String? = null, val remoteId: Int? = null, val revision: Long = 0, val createdAt: Long, @ColumnInfo(defaultValue = "''") val remoteCoverUrl: String = "", @ColumnInfo(defaultValue = "NULL") val role: String? = null, @ColumnInfo(defaultValue = "1") val subsLoaded: Boolean = true, @ColumnInfo(defaultValue="0") val locallyCreated: Boolean = false, @ColumnInfo(defaultValue="0") val deleted: Boolean = false)

@Entity(tableName = "local_subalbum", foreignKeys = [ForeignKey(entity = LocalAlbumEntity::class, parentColumns = ["localId"], childColumns = ["albumId"], onDelete = ForeignKey.CASCADE)], indices = [Index("albumId")])
data class LocalSubAlbumEntity(@PrimaryKey val localId: String, val albumId: String, val title: String, val remoteId: Int? = null, val revision: Long = 0, val createdAt: Long, @ColumnInfo(defaultValue = "1") val photosLoaded: Boolean = true, @ColumnInfo(defaultValue="0") val deleted: Boolean = false)

@Entity(tableName = "local_photo", foreignKeys = [ForeignKey(entity = LocalSubAlbumEntity::class, parentColumns = ["localId"], childColumns = ["subAlbumId"], onDelete = ForeignKey.CASCADE)], indices = [Index("subAlbumId")])
data class LocalPhotoEntity(@PrimaryKey val localId: String, val subAlbumId: String, val mediaName: String?, val remoteUrl: String = "", val remoteId: Int? = null, val description: String = "", val descriptionKnown: Boolean = true, val favorite: Boolean = false, val isLiked: Boolean = false, val likeCount: Int? = null, val revision: Long = 0, val createdAt: Long, @ColumnInfo(defaultValue="1") val commentsLoaded: Boolean = true, @ColumnInfo(defaultValue="0") val likesKnown: Boolean = false, val commentNextCursor: Int? = null, val cacheName: String? = null, @ColumnInfo(defaultValue="0") val deleted: Boolean = false)

@Entity(tableName = "local_memo", foreignKeys = [ForeignKey(entity = LocalPhotoEntity::class, parentColumns = ["localId"], childColumns = ["photoId"], onDelete = ForeignKey.CASCADE)], indices = [Index("photoId")])
data class LocalMemoEntity(@PrimaryKey val localId: String, val photoId: String, val text: String, val createdAt: Long)
