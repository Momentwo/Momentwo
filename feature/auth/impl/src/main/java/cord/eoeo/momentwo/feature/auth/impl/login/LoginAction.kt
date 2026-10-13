package cord.eoeo.momentwo.feature.auth.impl.login

import cord.eoeo.momentwo.core.common.UiError

internal sealed interface LoginAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data class Started(val automatic: Boolean) : LoginAction
    data object Succeeded : LoginAction
    data class Failed(val error: UiError) : LoginAction
    data object Finished : LoginAction
}
