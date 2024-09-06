package cord.eoeo.momentwo.data.friend.remote

import cord.eoeo.momentwo.data.friend.FriendDataSource
import cord.eoeo.momentwo.data.model.FriendList
import cord.eoeo.momentwo.data.model.FriendRequestResponse
import cord.eoeo.momentwo.data.model.ReceivedFriendRequestList
import cord.eoeo.momentwo.data.model.SentFriendRequestList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FriendRemoteDataSource(
    private val friendService: FriendService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FriendDataSource {
    override suspend fun sendFriendRequest(nickname: String): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                friendService.postFriendRequest(nickname)
            }
        }

    override suspend fun responseFriendRequest(
        nickname: String,
        isAccepted: Boolean,
    ): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                friendService.postResponseFriendRequest(FriendRequestResponse(nickname, isAccepted))
            }
        }

    override suspend fun cancelFriendRequest(nickname: String): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                friendService.deleteFriendRequest(nickname)
            }
        }

    override suspend fun getFriendList(): Result<FriendList> =
        runCatching {
            withContext(dispatcher) {
                friendService.getFriendList()
            }
        }

    override suspend fun getSentRequests(): Result<SentFriendRequestList> =
        runCatching {
            withContext(dispatcher) {
                friendService.getSentRequests()
            }
        }

    override suspend fun getReceivedRequests(): Result<ReceivedFriendRequestList> =
        runCatching {
            withContext(dispatcher) {
                friendService.getReceivedRequests()
            }
        }
}
