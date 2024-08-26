package cord.eoeo.momentwo.ui.model

enum class MemberAuth(
    val authString: String,
) {
    ADMIN("관리자"),
    SUB_ADMIN("부관리자"),
    MEMBER("멤버"),
    ;

    companion object {
        fun getAuth(str: String): MemberAuth =
            when (str) {
                "관리자" -> ADMIN
                "부관리자" -> SUB_ADMIN
                else -> MEMBER
            }
    }
}
