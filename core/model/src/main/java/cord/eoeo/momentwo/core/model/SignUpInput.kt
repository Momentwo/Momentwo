package cord.eoeo.momentwo.core.model

/** 입력 단계에서 정규화된 숫자 문자열을 보관한다. 전송 형식은 Data가 소유한다. */
data class SignUpInput(
    val name: String,
    val email: String,
    val password: String,
    val nickname: String,
    val birthdayDigits: String,
    val phoneDigits: String,
) {
    override fun toString(): String = "SignUpInput(redacted)"
}
