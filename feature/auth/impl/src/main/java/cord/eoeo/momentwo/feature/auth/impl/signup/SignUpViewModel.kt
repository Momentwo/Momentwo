package cord.eoeo.momentwo.feature.auth.impl.signup

import android.util.Log
import cord.eoeo.momentwo.core.common.ReducerViewModel
import cord.eoeo.momentwo.core.common.RequestKey
import cord.eoeo.momentwo.core.common.RequestPolicy
import cord.eoeo.momentwo.core.data.signup.SignUpRepository
import cord.eoeo.momentwo.core.data.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class SignUpViewModel @Inject constructor(
    private val signUpRepository: SignUpRepository,
) : ReducerViewModel<SignUpState, SignUpEvent, SignUpAction, SignUpEffect>(SignUpState(), SignUpReducer) {
    private data object Submit : RequestKey

    override fun handleEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.OnEmailEntered -> input(SignUpField.Email, event.email)
            is SignUpEvent.OnPasswordEntered -> input(SignUpField.Password, event.password)
            is SignUpEvent.OnPasswordCheckEntered -> input(SignUpField.PasswordCheck, event.passwordCheck)
            is SignUpEvent.OnNicknameEntered -> input(SignUpField.Nickname, event.nickname)
            is SignUpEvent.OnNameEntered -> input(SignUpField.Name, event.name)
            is SignUpEvent.OnBirthdayEntered -> input(SignUpField.Birthday, SignUpValidator.normalizeDigits(event.birthday, 8))
            is SignUpEvent.OnPhoneEntered -> input(SignUpField.Phone, SignUpValidator.normalizeDigits(event.phone, 11))
            SignUpEvent.OnNextClicked -> if (validate(SignUpValidator.firstPage(uiState.value))) sendEffect(SignUpEffect.ScrollToNextPage)
            SignUpEvent.OnAcceptClicked -> {
                if (!requests.isRunning(Submit) && validate(SignUpValidator.secondPage(uiState.value))) submit()
            }
            SignUpEvent.OnBack -> sendEffect(SignUpEffect.ScrollToPreviousPage)
            is SignUpEvent.OnError -> sendEffect(SignUpEffect.ShowSnackbar(event.errorMessage.ifBlank { "회원가입 요청에 실패했습니다" }))
        }
    }

    private fun input(field: SignUpField, value: String) = dispatch(SignUpAction.InputChanged(field, value))

    private fun validate(result: SignUpValidation): Boolean {
        dispatch(SignUpAction.Validated(result.errors))
        result.firstError?.let { sendEffect(SignUpEffect.ShowSnackbar(it)) }
        return result.firstError == null
    }

    private fun submit() {
        val submittedInput = uiState.value.toSignUpInput()
        requests.launchRequest(
            Submit, RequestPolicy.IgnoreWhileRunning,
            request = { signUpRepository.requestSignUp(submittedInput) },
            onStarted = { dispatch(SignUpAction.Started) },
            onSuccess = {
                dispatch(SignUpAction.Succeeded)
                emitEffect(SignUpEffect.NavigateToLogin)
            },
            onFailure = {
                Log.e("SignUp", "Sign-up request failed", it)
                dispatch(SignUpAction.Failed(it.toUiError()))
                emitEffect(SignUpEffect.ShowSnackbar("회원가입 요청에 실패했습니다"))
            },
            onFinished = { dispatch(SignUpAction.Finished) },
        )
    }
}
