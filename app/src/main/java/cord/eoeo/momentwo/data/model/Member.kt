package cord.eoeo.momentwo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import cord.eoeo.momentwo.core.model.MemberAuth
import cord.eoeo.momentwo.core.model.MemberItem

@JsonClass(generateAdapter = true)
data class InviteMembers(
    val albumId: Int,
    @Json(name = "inviteNicknames")
    val nicknameList: List<String>,
)

@JsonClass(generateAdapter = true)
data class AssignAdminToMember(
    val albumId: Int,
    val nickname: String,
)

@JsonClass(generateAdapter = true)
data class MemberInfo(
    val userId: Int,
    val nickname: String,
    val userProfileImage: String,
    val rules: String,
) {
    fun mapToMemberItem(): MemberItem =
        MemberItem(
            id = userId,
            nickname = nickname,
            profileImage = userProfileImage,
            auth = MemberAuth.fromRoleString(rules),
        )
}

@JsonClass(generateAdapter = true)
data class MemberList(
    @Json(name = "albumMember")
    val memberList: List<MemberInfo>,
)

@JsonClass(generateAdapter = true)
data class EditMembers(
    val albumId: Int,
    @Json(name = "editMemberList")
    val memberMap: Map<String, String>,
)
