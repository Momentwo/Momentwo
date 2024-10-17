package cord.eoeo.momentwo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import cord.eoeo.momentwo.data.photo.local.PhotoDao
import cord.eoeo.momentwo.data.photo.local.PhotoRemoteKeyDao
import cord.eoeo.momentwo.data.photo.local.entity.PhotoEntity
import cord.eoeo.momentwo.data.photo.local.entity.PhotoRemoteKeyEntity

@Database(
    entities = [
        PhotoEntity::class,
        PhotoRemoteKeyEntity::class,
    ],
    version = 1
)
abstract class MomentwoDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao

    abstract fun photoRemoteKeyDao(): PhotoRemoteKeyDao
}
