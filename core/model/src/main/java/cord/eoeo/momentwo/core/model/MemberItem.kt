package cord.eoeo.momentwo.core.model

data class MemberItem(
    val id: Int,
    val nickname: String,
    val profileImage: String,
    val auth: MemberAuth,
)
