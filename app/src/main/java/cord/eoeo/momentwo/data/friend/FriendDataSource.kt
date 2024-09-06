package cord.eoeo.momentwo.data.friend

import cord.eoeo.momentwo.data.model.FriendList
import cord.eoeo.momentwo.data.model.ReceivedFriendRequestList
import cord.eoeo.momentwo.data.model.SentFriendRequestList

interface FriendDataSource {
    suspend fun sendFriendRequest(nickname: String): Result<Unit>

    suspend fun responseFriendRequest(
        nickname: String,
        isAccepted: Boolean,
    ): Result<Unit>

    suspend fun cancelFriendRequest(nickname: String): Result<Unit>

    suspend fun getFriendList(): Result<FriendList>

    suspend fun getSentRequests(): Result<SentFriendRequestList>

    suspend fun getReceivedRequests(): Result<ReceivedFriendRequestList>
}
