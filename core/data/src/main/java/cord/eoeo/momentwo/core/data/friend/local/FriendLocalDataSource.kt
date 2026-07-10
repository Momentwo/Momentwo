package cord.eoeo.momentwo.core.data.friend.local

import androidx.paging.PagingSource
import cord.eoeo.momentwo.core.data.friend.FriendDataSource
import cord.eoeo.momentwo.core.database.FriendDao
import cord.eoeo.momentwo.core.database.FriendRemoteKeyDao
import cord.eoeo.momentwo.core.database.FriendEntity
import cord.eoeo.momentwo.core.database.FriendRemoteKeyEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class FriendLocalDataSource(
    private val friendDao: FriendDao,
    private val friendRemoteKeyDao: FriendRemoteKeyDao,
    private val dispatcher: CoroutineDispatcher
) : FriendDataSource.Local {
    override fun getPhotoPagingSource(): PagingSource<Int, FriendEntity> =
        friendDao.getFriendPagingSource()

    override suspend fun getFriendList(): Result<List<FriendEntity>> =
        runCatching {
            withContext(dispatcher) {
                friendDao.getFriendList()
            }
        }

    override suspend fun insertFriends(friends: List<FriendEntity>) {
        withContext(dispatcher) {
            friendDao.insertAll(*friends.toTypedArray())
        }
    }

    override suspend fun deleteByNicknames(friendNicknames: List<String>) {
        withContext(dispatcher) {
            friendDao.deleteByNicknames(friendNicknames)
        }
    }

    override suspend fun deleteAll() {
        withContext(dispatcher) {
            friendDao.deleteAll()
        }
    }

    override suspend fun getLastKey(): FriendRemoteKeyEntity? =
        withContext(dispatcher) {
            friendRemoteKeyDao.getLastKey()
        }

    override suspend fun insertKey(key: FriendRemoteKeyEntity) {
        withContext(dispatcher) {
            friendRemoteKeyDao.insertKey(key)
        }
    }

    override suspend fun clearKeys() {
        withContext(dispatcher) {
            friendRemoteKeyDao.clearKeys()
        }
    }
}
