package cord.eoeo.momentwo.core.data.friend

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

        friendRemoteDataSource
            .getFriendPage(pageSize, lastKey?.nextCursor ?: 0)
            .onSuccess { friendPage ->
                if (friendPage.nextCursor == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                if (loadType == LoadType.REFRESH) {
                    friendLocalDataSource.deleteAll()
                    friendLocalDataSource.clearKeys()
                }

                friendLocalDataSource.insertFriends(
                    friendPage.friends.map { friendInfo ->
                        FriendEntity(
                            nickname = friendInfo.nickname,
                            userProfileImage = friendInfo.userProfileImage,
                        )
                    }
                )

                friendLocalDataSource.insertKey(
                    FriendRemoteKeyEntity(
                        lastUpdated = System.currentTimeMillis(),
                        nextCursor = friendPage.nextCursor,
                    )
                )

                return MediatorResult.Success(endOfPaginationReached = false)
            }.onFailure { exception ->
                return MediatorResult.Error(exception)
            }
        return MediatorResult.Error(Exception("Unknown Error"))
    }
}
