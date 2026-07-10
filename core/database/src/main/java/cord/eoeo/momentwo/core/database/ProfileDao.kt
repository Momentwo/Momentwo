package cord.eoeo.momentwo.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cord.eoeo.momentwo.core.database.ProfileEntity

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile LIMIT 1")
    suspend fun getProfile(): ProfileEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(profile: ProfileEntity)
}
