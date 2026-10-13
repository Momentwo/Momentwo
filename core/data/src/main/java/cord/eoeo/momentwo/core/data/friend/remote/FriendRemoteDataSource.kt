package cord.eoeo.momentwo.core.data.friend.remote

import cord.eoeo.momentwo.core.common.runSuspendCatching

import cord.eoeo.momentwo.core.data.friend.FriendDataSource
import cord.eoeo.momentwo.core.data.model.FriendPage
import cord.eoeo.momentwo.core.data.model.FriendRequestResponse
import cord.eoeo.momentwo.core.data.model.ReceivedFriendRequestList
import cord.eoeo.momentwo.core.data.model.SearchUser
import cord.eoeo.momentwo.core.data.model.SentFriendRequestList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class FriendRemoteDataSource(
    private val friendService: FriendService,
    private val dispatcher: CoroutineDispatcher,
) : FriendDataSource.Remote {
    override suspend fun sendFriendRequest(nickname: String): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                friendService.postFriendRequest(nickname)
            }
        }

    override suspend fun responseFriendRequest(
        nickname: String,
        isAccepted: Boolean,
    ): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                friendService.postResponseFriendRequest(FriendRequestResponse(nickname, isAccepted))
            }
        }

    override suspend fun cancelFriendRequest(userId: Int): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                friendService.deleteFriendRequest(userId)
            }
        }

    override suspend fun getFriendPage(
        size: Int,
        cursor: Int,
    ): Result<FriendPage> =
        runSuspendCatching {
            withContext(dispatcher) {
                friendService.getFriendList(size, cursor)
            }
        }

    override suspend fun getSentRequests(): Result<SentFriendRequestList> =
        runSuspendCatching {
            withContext(dispatcher) {
                friendService.getSentRequests()
            }
        }

    override suspend fun getReceivedRequests(): Result<ReceivedFriendRequestList> =
        runSuspendCatching {
            withContext(dispatcher) {
                friendService.getReceivedRequests()
            }
        }

    override suspend fun getSearchUsers(
        nickname: String,
        page: Int,
        size: Int,
    ): Result<SearchUser> =
        runSuspendCatching {
            withContext(dispatcher) {
                friendService.getSearchUsers(nickname, page, size)
            }
        }
}
