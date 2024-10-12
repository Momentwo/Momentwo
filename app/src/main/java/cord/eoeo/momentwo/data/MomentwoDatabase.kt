package cord.eoeo.momentwo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import cord.eoeo.momentwo.data.photo.local.PhotoDao
import cord.eoeo.momentwo.data.photo.local.PhotoEntity

@Database(entities = [PhotoEntity::class], version = 1)
abstract class MomentwoDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}
