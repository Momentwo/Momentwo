package cord.eoeo.momentwo.core.model

data class FriendRequestItem(
    val userId: Int,
    val nickname: String,
    val userProfileImage: String,
    val isUpdated: Boolean,
)
