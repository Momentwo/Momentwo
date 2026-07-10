package cord.eoeo.momentwo.core.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cord.eoeo.momentwo.core.database.FriendEntity

@Dao
interface FriendDao {
    @Query("SELECT * FROM friend ORDER BY nickname")
    fun getFriendPagingSource(): PagingSource<Int, FriendEntity>

    @Query("SELECT * FROM friend ORDER BY nickname")
    suspend fun getFriendList(): List<FriendEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg friends: FriendEntity)

    @Query("DELETE FROM friend WHERE nickname IN (:friendNicknames)")
    suspend fun deleteByNicknames(friendNicknames: List<String>)

    @Query("DELETE FROM friend")
    suspend fun deleteAll()
}
