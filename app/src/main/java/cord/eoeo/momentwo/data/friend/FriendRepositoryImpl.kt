package cord.eoeo.momentwo.data.friend

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import cord.eoeo.momentwo.data.friend.remote.SearchUserPagingSource
import cord.eoeo.momentwo.domain.friend.FriendRepository
import cord.eoeo.momentwo.core.model.FriendItem
import cord.eoeo.momentwo.core.model.FriendRequestItem
import cord.eoeo.momentwo.core.model.UserItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FriendRepositoryImpl(
    private val friendRemoteDataSource: FriendDataSource.Remote,
    private val friendLocalDataSource: FriendDataSource.Local,
    private val friendRemoteMediator: FriendRemoteMediator,
) : FriendRepository {
    override suspend fun sendFriendRequest(nickname: String): Result<Unit> =
        friendRemoteDataSource.sendFriendRequest(nickname)

    override suspend fun responseFriendRequest(nickname: String, isAccepted: Boolean): Result<Unit> =
        friendRemoteDataSource.responseFriendRequest(nickname, isAccepted)

    override suspend fun cancelFriendRequest(userId: Int): Result<Unit> =
        friendRemoteDataSource.cancelFriendRequest(userId)

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getFriendPage(size: Int): Flow<PagingData<FriendItem>> {
        friendRemoteMediator.setParams(size)

        return Pager(
            config = PagingConfig(pageSize = size),
            remoteMediator = friendRemoteMediator,
            pagingSourceFactory = { friendLocalDataSource.getPhotoPagingSource() },
        ).flow.map { friendPagingData ->
            friendPagingData.map { friendEntity ->
                friendEntity.mapToFriendItem()
            }
        }
    }

    override suspend fun getFriendList(): Result<List<FriendItem>> =
        friendLocalDataSource.getFriendList().map { friendList ->
            friendList.map { friendEntity ->
                friendEntity.mapToFriendItem()
            }
        }

    override suspend fun getSentRequests(): Result<List<FriendRequestItem>> =
        friendRemoteDataSource
            .getSentRequests()
            .map { sentRequests ->
                sentRequests.sentList.map {
                    it.mapToFriendRequestItem()
                }
            }

    override suspend fun getReceivedRequests(): Result<List<FriendRequestItem>> =
        friendRemoteDataSource
            .getReceivedRequests()
            .map { receivedRequests ->
                receivedRequests.receivedList.map {
                    it.mapToFriendRequestItem()
                }
            }

    override suspend fun getSearchUsers(nickname: String, size: Int): Flow<PagingData<UserItem>> =
        Pager(
            config = PagingConfig(pageSize = size),
            pagingSourceFactory = { SearchUserPagingSource(friendRemoteDataSource, nickname, size) }
        ).flow.map { searchUserPagingData ->
            searchUserPagingData.map { searchUserInfo ->
                searchUserInfo.mapToUserItem()
            }
        }
}
