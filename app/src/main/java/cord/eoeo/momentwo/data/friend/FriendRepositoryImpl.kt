package cord.eoeo.momentwo.data.friend

import cord.eoeo.momentwo.domain.friend.FriendRepository
import cord.eoeo.momentwo.ui.model.FriendItem
import cord.eoeo.momentwo.ui.model.FriendRequestItem

class FriendRepositoryImpl(
    private val friendRemoteDataSource: FriendDataSource,
) : FriendRepository {
    override suspend fun sendFriendRequest(nickname: String): Result<Unit> {
        return friendRemoteDataSource.sendFriendRequest(nickname)
        // return Result.success(Unit)
    }

    override suspend fun responseFriendRequest(
        nickname: String,
        isAccepted: Boolean,
    ): Result<Unit> {
        return friendRemoteDataSource.responseFriendRequest(nickname, isAccepted)
        // return Result.success(Unit)
    }

    override suspend fun cancelFriendRequest(nickname: String): Result<Unit> {
        return friendRemoteDataSource.cancelFriendRequest(nickname)
        // return Result.success(Unit)
    }

    override suspend fun getFriendList(): Result<List<FriendItem>> {
        return friendRemoteDataSource
            .getFriendList()
            .map { friendList ->
                friendList.friends.map {
                    it.mapToFriendItem()
                }
            }
        /*return Result.success(
            listOf(
                FriendItem("user1"),
                FriendItem("user2"),
                FriendItem("user3"),
                FriendItem("user4"),
                FriendItem("user5"),
            ),
        )*/
    }

    override suspend fun getSentRequests(): Result<List<FriendRequestItem>> {
        return friendRemoteDataSource
            .getSentRequests()
            .map { sentRequests ->
                sentRequests.sentList.map {
                    it.mapToFriendRequestItem()
                }
            }
        /*return Result.success(
            listOf(
                FriendRequestItem("user6", false),
                FriendRequestItem("user7", false),
                FriendRequestItem("user8", false),
            ),
        )*/
    }

    override suspend fun getReceivedRequests(): Result<List<FriendRequestItem>> {
        return friendRemoteDataSource
            .getReceivedRequests()
            .map { receivedRequests ->
                receivedRequests.receivedList.map {
                    it.mapToFriendRequestItem()
                }
            }
        /*return Result.success(
            listOf(
                FriendRequestItem("user9", false),
                FriendRequestItem("user10", false),
                FriendRequestItem("user11", false),
            ),
        )*/
    }
}
