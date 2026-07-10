package cord.eoeo.momentwo.core.data.friend

import androidx.paging.PagingSource
import cord.eoeo.momentwo.core.database.FriendEntity
import cord.eoeo.momentwo.core.database.FriendRemoteKeyEntity
import cord.eoeo.momentwo.core.data.model.FriendPage
import cord.eoeo.momentwo.core.data.model.ReceivedFriendRequestList
import cord.eoeo.momentwo.core.data.model.SearchUser
import cord.eoeo.momentwo.core.data.model.SentFriendRequestList

interface FriendDataSource {
    interface Remote {
        suspend fun sendFriendRequest(nickname: String): Result<Unit>

        suspend fun responseFriendRequest(
            nickname: String,
            isAccepted: Boolean,
        ): Result<Unit>

        suspend fun cancelFriendRequest(userId: Int): Result<Unit>

        suspend fun getFriendPage(
            size: Int,
            cursor: Int,
        ): Result<FriendPage>

        suspend fun getSentRequests(): Result<SentFriendRequestList>

        suspend fun getReceivedRequests(): Result<ReceivedFriendRequestList>

        suspend fun getSearchUsers(
            nickname: String,
            page: Int,
            size: Int,
        ): Result<SearchUser>
    }

    interface Local {
        fun getPhotoPagingSource(): PagingSource<Int, FriendEntity>

        suspend fun getFriendList(): Result<List<FriendEntity>>

        suspend fun insertFriends(friends: List<FriendEntity>)

        suspend fun deleteByNicknames(friendNicknames: List<String>)

        suspend fun deleteAll()

        suspend fun getLastKey(): FriendRemoteKeyEntity?

        suspend fun insertKey(key: FriendRemoteKeyEntity)

        suspend fun clearKeys()
    }
}
