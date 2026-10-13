package cord.eoeo.momentwo.feature.auth.impl.login

import cord.eoeo.momentwo.core.common.UiState
import cord.eoeo.momentwo.core.common.UiError

internal sealed interface LoginStatus {
    data object Idle : LoginStatus
    data class Submitting(val automatic: Boolean) : LoginStatus
    data object Succeeded : LoginStatus
    data class Failed(val error: UiError) : LoginStatus
}

internal data class LoginState(
    val email: String = "",
    val password: String = "",
    val status: LoginStatus = LoginStatus.Idle,
) : UiState {
    val isLoading: Boolean get() = status is LoginStatus.Submitting
    val isSuccess: Boolean get() = status == LoginStatus.Succeeded
    val isError: Boolean get() = status is LoginStatus.Failed
    val isSubmittingCredentials: Boolean get() = status == LoginStatus.Submitting(automatic = false)
}
