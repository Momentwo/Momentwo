package cord.eoeo.momentwo.core.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import cord.eoeo.momentwo.core.model.FriendRequestItem
import cord.eoeo.momentwo.core.model.UserItem

@JsonClass(generateAdapter = true)
data class FriendRequestResponse(
    val nickname: String,
    val accept: Boolean,
)

@JsonClass(generateAdapter = true)
data class FriendInfo(
    val userId: Int,
    val nickname: String,
    val userProfileImage: String,
) {
    fun mapToFriendRequestItem(): FriendRequestItem =
        FriendRequestItem(
            userId = userId,
            nickname = nickname,
            userProfileImage = userProfileImage,
            isUpdated = false,
        )
}

@JsonClass(generateAdapter = true)
data class FriendPage(
    @Json(name = "friendshipAllListDtoList")
    val friends: List<FriendInfo>,
    val nextCursor: Int?,
)

@JsonClass(generateAdapter = true)
data class SentFriendRequestList(
    @Json(name = "friendshipSendList")
    val sentList: List<FriendInfo>,
)

@JsonClass(generateAdapter = true)
data class ReceivedFriendRequestList(
    @Json(name = "friendshipReceiveList")
    val receivedList: List<FriendInfo>,
)

@JsonClass(generateAdapter = true)
data class SearchUser(
    val searchUsers: List<SearchUserInfo>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

@JsonClass(generateAdapter = true)
data class SearchUserInfo(
    val id: Int,
    val nickname: String,
    val userProfileImage: String?
) {
    fun mapToUserItem(): UserItem =
        UserItem(
            id = id,
            nickname = nickname,
            userProfileImage = userProfileImage ?: "",
        )
}
