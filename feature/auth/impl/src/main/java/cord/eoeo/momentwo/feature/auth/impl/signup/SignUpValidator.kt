package cord.eoeo.momentwo.feature.auth.impl.signup

internal data class SignUpValidation(val errors: Map<SignUpField, String?>) {
    val firstError: String? get() = errors.values.firstOrNull { it != null }
}

internal object SignUpValidator {
    private val emailRegex = Regex("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$")
    private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&])[A-Za-z[0-9]$@$!%*#?&]{8,20}$")
    fun normalizeDigits(value: String, limit: Int): String = value.filter { it in '0'..'9' }.take(limit)
    fun firstPage(state: SignUpState) = validate(state, listOf(SignUpField.Email, SignUpField.Password, SignUpField.PasswordCheck))
    fun secondPage(state: SignUpState) = validate(state, listOf(SignUpField.Nickname, SignUpField.Name, SignUpField.Birthday, SignUpField.Phone))

    private fun validate(state: SignUpState, fields: List<SignUpField>): SignUpValidation {
        val errors = linkedMapOf<SignUpField, String?>()
        for (field in fields) {
            val value = state.field(field).value
            val error = when (field) {
                SignUpField.Email -> when {
                    value.isEmpty() -> "이메일 입력을 확인하세요"
                    !emailRegex.matches(value) -> "이메일 형식이 올바르지 않습니다"
                    else -> null
                }
                SignUpField.Password -> when {
                    value.isEmpty() -> "비밀번호 입력을 확인하세요"
                    !passwordRegex.matches(value) -> "비밀번호 형식이 올바르지 않습니다"
                    else -> null
                }
                SignUpField.PasswordCheck -> if (value != state.password.value) "비밀번호가 일치하지 않습니다" else null
                SignUpField.Nickname -> if (value.isEmpty()) "별명 입력을 확인하세요" else null
                SignUpField.Name -> if (value.isEmpty()) "이름 입력을 확인하세요" else null
                SignUpField.Birthday -> if (value.length < 8) "생년월일 입력을 확인하세요" else null
                SignUpField.Phone -> if (value.length < 11) "전화번호 입력을 확인하세요" else null
            }
            errors[field] = error
            if (error != null) break // 기존 첫 오류 우선 검증 순서를 유지한다.
        }
        return SignUpValidation(errors)
    }
}
