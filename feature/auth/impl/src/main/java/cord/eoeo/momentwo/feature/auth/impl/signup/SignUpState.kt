package cord.eoeo.momentwo.feature.auth.impl.signup

import cord.eoeo.momentwo.core.common.UiState
import cord.eoeo.momentwo.core.common.UiError

internal data class SignUpFieldState(val value: String = "", val isError: Boolean = false, val errorMessage: String = "")
internal enum class SignUpField { Email, Password, PasswordCheck, Nickname, Name, Birthday, Phone }
internal sealed interface SignUpStatus {
    data object Idle : SignUpStatus
    data object Submitting : SignUpStatus
    data object Succeeded : SignUpStatus
    data class Failed(val error: UiError) : SignUpStatus
}
internal data class SignUpState(
    val email: SignUpFieldState = SignUpFieldState(),
    val password: SignUpFieldState = SignUpFieldState(),
    val passwordCheck: SignUpFieldState = SignUpFieldState(),
    val nickname: SignUpFieldState = SignUpFieldState(),
    val name: SignUpFieldState = SignUpFieldState(),
    val birthday: SignUpFieldState = SignUpFieldState(),
    val phone: SignUpFieldState = SignUpFieldState(),
    val status: SignUpStatus = SignUpStatus.Idle,
) : UiState {
    val isLoading get() = status == SignUpStatus.Submitting
    val isSuccess get() = status == SignUpStatus.Succeeded
    val isError get() = status is SignUpStatus.Failed
    fun field(field: SignUpField): SignUpFieldState = when (field) {
        SignUpField.Email -> email
        SignUpField.Password -> password
        SignUpField.PasswordCheck -> passwordCheck
        SignUpField.Nickname -> nickname
        SignUpField.Name -> name
        SignUpField.Birthday -> birthday
        SignUpField.Phone -> phone
    }
}
