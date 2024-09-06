package cord.eoeo.momentwo.data.friend

import cord.eoeo.momentwo.ui.model.FriendItem
import cord.eoeo.momentwo.ui.model.FriendRequestItem

interface FriendRepository {
    suspend fun sendFriendRequest(nickname: String): Result<Unit>

    suspend fun responseFriendRequest(
        nickname: String,
        isAccepted: Boolean,
    ): Result<Unit>

    suspend fun cancelFriendRequest(nickname: String): Result<Unit>

    suspend fun getFriendList(): Result<List<FriendItem>>

    suspend fun getSentRequests(): Result<List<FriendRequestItem>>

    suspend fun getReceivedRequests(): Result<List<FriendRequestItem>>
}
