package cord.eoeo.momentwo.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cord.eoeo.momentwo.core.database.FriendRemoteKeyEntity

@Dao
interface FriendRemoteKeyDao {
    @Query("SELECT * FROM friend_key WHERE ownerId = :ownerId AND id = (SELECT MAX(id) FROM friend_key WHERE ownerId = :ownerId)")
    suspend fun getLastKey(ownerId: String = ""): FriendRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: FriendRemoteKeyEntity)

    @Query("DELETE FROM friend_key WHERE ownerId = :ownerId")
    suspend fun clearKeys(ownerId: String = "")
}
