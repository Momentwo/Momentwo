package cord.eoeo.momentwo.core.data.friend.local

import cord.eoeo.momentwo.core.common.runSuspendCatching

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
    private val dispatcher: CoroutineDispatcher,
    private val ownerId: String = ""
) : FriendDataSource.Local {
    override fun getPhotoPagingSource(): PagingSource<Int, FriendEntity> =
        friendDao.getFriendPagingSource(ownerId)

    override suspend fun getFriendList(): Result<List<FriendEntity>> =
        runSuspendCatching {
            withContext(dispatcher) {
                friendDao.getFriendList(ownerId)
            }
        }

    override suspend fun insertFriends(friends: List<FriendEntity>) {
        withContext(dispatcher) {
            friendDao.insertAll(*friends.map { it.copy(ownerId = ownerId) }.toTypedArray())
        }
    }

    override suspend fun deleteByNicknames(friendNicknames: List<String>) {
        withContext(dispatcher) {
            friendDao.deleteByNicknames(friendNicknames,ownerId)
        }
    }

    override suspend fun deleteAll() {
        withContext(dispatcher) {
            friendDao.deleteAll(ownerId)
        }
    }

    override suspend fun getLastKey(): FriendRemoteKeyEntity? =
        withContext(dispatcher) {
            friendRemoteKeyDao.getLastKey(ownerId)
        }

    override suspend fun insertKey(key: FriendRemoteKeyEntity) {
        withContext(dispatcher) {
            friendRemoteKeyDao.insertKey(key.copy(ownerId = ownerId))
        }
    }

    override suspend fun clearKeys() {
        withContext(dispatcher) {
            friendRemoteKeyDao.clearKeys(ownerId)
        }
    }
}
