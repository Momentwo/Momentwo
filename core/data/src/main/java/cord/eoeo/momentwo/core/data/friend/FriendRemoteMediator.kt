package cord.eoeo.momentwo.core.data.friend

import cord.eoeo.momentwo.core.common.runSuspendCatching
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import cord.eoeo.momentwo.core.database.FriendEntity
import cord.eoeo.momentwo.core.database.FriendRemoteKeyEntity
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class FriendRemoteMediator(
    private val friendRemoteDataSource: FriendDataSource.Remote,
    private val friendLocalDataSource: FriendDataSource.Local,
) : RemoteMediator<Int, FriendEntity>() {
    private val timeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)
    private var pageSize = 10

    fun setParams(pageSize: Int) {
        this.pageSize = pageSize
    }

    override suspend fun initialize(): InitializeAction {
        val lastKey = friendLocalDataSource.getLastKey()

        return if (
            lastKey == null ||
            System.currentTimeMillis() - lastKey.lastUpdated > timeout
        ) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, FriendEntity>): MediatorResult {
        val lastKey = when (loadType) {
            LoadType.REFRESH -> {
                null
            }

            LoadType.PREPEND -> {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                val last = friendLocalDataSource.getLastKey()
                if (last?.nextCursor == null) return MediatorResult.Success(endOfPaginationReached = true)
                last
            }
        }

        return runSuspendCatching {
            val page = friendRemoteDataSource.getFriendPage(pageSize, lastKey?.nextCursor ?: 0).getOrThrow()
            if (loadType == LoadType.REFRESH) {
                friendLocalDataSource.deleteAll()
                friendLocalDataSource.clearKeys()
            }
            friendLocalDataSource.insertFriends(page.friends.map { FriendEntity(nickname = it.nickname, userProfileImage = it.userProfileImage) })
            friendLocalDataSource.insertKey(FriendRemoteKeyEntity(lastUpdated = System.currentTimeMillis(), nextCursor = page.nextCursor))
            MediatorResult.Success(endOfPaginationReached = page.nextCursor == null)
        }.getOrElse { MediatorResult.Error(it) }
    }
}
