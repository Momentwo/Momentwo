package cord.eoeo.momentwo.feature.auth.impl.signup

import cord.eoeo.momentwo.core.common.Reducer

internal object SignUpReducer : Reducer<SignUpState, SignUpAction> {
    override fun reduce(state: SignUpState, action: SignUpAction): SignUpState = when (action) {
        is SignUpAction.InputChanged -> state.withField(action.field, state.field(action.field).copy(value = action.value))
        is SignUpAction.Validated -> action.errors.entries.fold(state) { current, (field, error) ->
            current.withField(field, current.field(field).copy(isError = error != null, errorMessage = error.orEmpty()))
        }
        SignUpAction.Started -> state.copy(status = SignUpStatus.Submitting)
        SignUpAction.Succeeded -> state.copy(status = SignUpStatus.Succeeded)
        is SignUpAction.Failed -> state.copy(status = SignUpStatus.Failed(action.error))
        SignUpAction.Finished -> if (state.isLoading) state.copy(status = SignUpStatus.Idle) else state
    }

    private fun SignUpState.withField(field: SignUpField, value: SignUpFieldState): SignUpState = when (field) {
        SignUpField.Email -> copy(email = value)
        SignUpField.Password -> copy(password = value)
        SignUpField.PasswordCheck -> copy(passwordCheck = value)
        SignUpField.Nickname -> copy(nickname = value)
        SignUpField.Name -> copy(name = value)
        SignUpField.Birthday -> copy(birthday = value)
        SignUpField.Phone -> copy(phone = value)
    }
}
