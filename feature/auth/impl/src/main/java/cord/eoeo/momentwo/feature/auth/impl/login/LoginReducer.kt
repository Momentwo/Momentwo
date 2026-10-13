package cord.eoeo.momentwo.feature.auth.impl.login

import cord.eoeo.momentwo.core.common.Reducer

internal object LoginReducer : Reducer<LoginState, LoginAction> {
    override fun reduce(state: LoginState, action: LoginAction): LoginState = when (action) {
        is LoginAction.EmailChanged -> state.copy(email = action.email)
        is LoginAction.PasswordChanged -> state.copy(password = action.password)
        is LoginAction.Started -> state.copy(status = LoginStatus.Submitting(action.automatic))
        LoginAction.Succeeded -> state.copy(status = LoginStatus.Succeeded)
        is LoginAction.Failed -> state.copy(status = LoginStatus.Failed(action.error))
        LoginAction.Finished -> if (state.isLoading) state.copy(status = LoginStatus.Idle) else state
    }
}
