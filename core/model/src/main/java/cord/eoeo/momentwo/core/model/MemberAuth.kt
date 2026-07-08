package cord.eoeo.momentwo.core.model

enum class MemberAuth(
    val roleString: String,
    val authString: String,
) {
    ADMIN("ROLE_ALBUM_ADMIN", "관리자"),
    SUB_ADMIN("ROLE_ALBUM_SUB_ADMIN", "부관리자"),
    MEMBER("ROLE_ALBUM_COMMON_MEMBER", "멤버"),
    ;

    companion object {
        fun fromRoleString(role: String): MemberAuth =
            when (role) {
                "ROLE_ALBUM_ADMIN" -> ADMIN
                "ROLE_ALBUM_SUB_ADMIN" -> SUB_ADMIN
                else -> MEMBER
            }

        fun getAuth(str: String): MemberAuth =
            when (str) {
                "관리자" -> ADMIN
                "부관리자" -> SUB_ADMIN
                else -> MEMBER
            }
    }
}
