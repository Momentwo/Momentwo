package cord.eoeo.momentwo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import cord.eoeo.momentwo.ui.model.FriendItem
import cord.eoeo.momentwo.ui.model.FriendRequestItem

@JsonClass(generateAdapter = true)
data class FriendRequestResponse(
    val nickname: String,
    val accept: Boolean,
)

@JsonClass(generateAdapter = true)
data class FriendInfo(
    val nickname: String,
) {
    fun mapToFriendItem(): FriendItem =
        FriendItem(
            nickname = nickname,
        )

    fun mapToFriendRequestItem(): FriendRequestItem =
        FriendRequestItem(
            nickname = nickname,
            isUpdated = false,
        )
}

@JsonClass(generateAdapter = true)
data class FriendList(
    @Json(name = "friendshipAllListDtoList")
    val friends: List<FriendInfo>,
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
