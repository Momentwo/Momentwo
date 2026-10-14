package cord.eoeo.momentwo.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import cord.eoeo.momentwo.core.database.FriendDao
import cord.eoeo.momentwo.core.database.FriendRemoteKeyDao
import cord.eoeo.momentwo.core.database.FriendEntity
import cord.eoeo.momentwo.core.database.FriendRemoteKeyEntity
import cord.eoeo.momentwo.core.database.ProfileDao
import cord.eoeo.momentwo.core.database.ProfileEntity
import cord.eoeo.momentwo.core.database.PhotoDao
import cord.eoeo.momentwo.core.database.PhotoRemoteKeyDao
import cord.eoeo.momentwo.core.database.PhotoEntity
import cord.eoeo.momentwo.core.database.PhotoRemoteKeyEntity

@Database(
    entities = [
        SyncBatchEntity::class, SyncJobEntity::class, SyncLinkEntity::class, SyncMediaPinEntity::class, AccountCommentEntity::class, LibraryEntity::class, LocalAlbumEntity::class, LocalSubAlbumEntity::class, LocalPhotoEntity::class, LocalMemoEntity::class,
        PhotoEntity::class,
        PhotoRemoteKeyEntity::class,
        FriendEntity::class,
        FriendRemoteKeyEntity::class,
        ProfileEntity::class,
    ],
    version = 4,
)
abstract class MomentwoDatabase : RoomDatabase() {
    abstract fun syncDao(): SyncDao
    abstract fun libraryDao(): LibraryDao

    abstract fun photoDao(): PhotoDao

    abstract fun photoRemoteKeyDao(): PhotoRemoteKeyDao

    abstract fun friendDao(): FriendDao

    abstract fun friendRemoteKeyDao(): FriendRemoteKeyDao

    abstract fun profileDao(): ProfileDao
}
