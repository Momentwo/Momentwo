package cord.eoeo.momentwo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import cord.eoeo.momentwo.ui.model.MemberAuth
import cord.eoeo.momentwo.ui.model.MemberItem

@JsonClass(generateAdapter = true)
data class InviteMembers(
    @Json(name = "inviteNicknames")
    val nicknameList: List<String>,
)

@JsonClass(generateAdapter = true)
data class KickMembers(
    @Json(name = "kickMemberList")
    val memberList: List<String>,
)

@JsonClass(generateAdapter = true)
enum class MemberRule {
    ROLE_ALBUM_ADMIN,
    ROLE_ALBUM_SUB_ADMIN,
    ROLE_ALBUM_COMMON_MEMBER,
    ;

    fun mapToMemberAuth(): MemberAuth =
        when (this) {
            ROLE_ALBUM_ADMIN -> MemberAuth.ADMIN
            ROLE_ALBUM_SUB_ADMIN -> MemberAuth.SUB_ADMIN
            ROLE_ALBUM_COMMON_MEMBER -> MemberAuth.MEMBER
        }

    companion object {
        fun memberAuthToRule(memberAuth: MemberAuth): MemberRule =
            when (memberAuth) {
                MemberAuth.ADMIN -> ROLE_ALBUM_ADMIN
                MemberAuth.SUB_ADMIN -> ROLE_ALBUM_SUB_ADMIN
                MemberAuth.MEMBER -> ROLE_ALBUM_COMMON_MEMBER
            }
    }
}

@JsonClass(generateAdapter = true)
data class MemberInfo(
    val nickname: String,
    val rules: MemberRule,
) {
    fun mapToMemberItem(): MemberItem =
        MemberItem(
            nickname = nickname,
            auth = rules.mapToMemberAuth(),
        )
}

@JsonClass(generateAdapter = true)
data class MemberList(
    @Json(name = "albumMember")
    val memberList: List<MemberInfo>,
)

@JsonClass(generateAdapter = true)
data class EditMembers(
    @Json(name = "editMemberList")
    val memberMap: Map<String, MemberRule>,
)
