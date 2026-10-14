package cord.eoeo.momentwo.core.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cord.eoeo.momentwo.core.database.FriendEntity

@Dao
interface FriendDao {
    @Query("SELECT * FROM friend WHERE ownerId = :ownerId ORDER BY nickname")
    fun getFriendPagingSource(ownerId: String = ""): PagingSource<Int, FriendEntity>

    @Query("SELECT * FROM friend WHERE ownerId = :ownerId ORDER BY nickname")
    suspend fun getFriendList(ownerId: String = ""): List<FriendEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg friends: FriendEntity)

    @Query("DELETE FROM friend WHERE ownerId = :ownerId AND nickname IN (:friendNicknames)")
    suspend fun deleteByNicknames(friendNicknames: List<String>, ownerId: String = "")

    @Query("DELETE FROM friend WHERE ownerId = :ownerId")
    suspend fun deleteAll(ownerId: String = "")
}
