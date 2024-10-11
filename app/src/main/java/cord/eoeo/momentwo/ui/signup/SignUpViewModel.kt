package cord.eoeo.momentwo.ui.signup

import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.data.signup.SignUpRepository
import cord.eoeo.momentwo.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpRepository: SignUpRepository
) : BaseViewModel<SignUpContract.State, SignUpContract.Event, SignUpContract.Effect>() {
    private val emailRegex = Regex("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$")
    private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&])[A-Za-z[0-9]$@$!%*#?&]{8,20}$")
    private val numberRegex = Regex("[^0-9]")

    override fun createInitialState(): SignUpContract.State = SignUpContract.State()

    override fun handleEvent(newEvent: SignUpContract.Event) {
        when (newEvent) {
            is SignUpContract.Event.OnEmailEntered -> {
                with(uiState.value) { setState(copy(email = email.copy(value = newEvent.email))) }
            }

            is SignUpContract.Event.OnPasswordEntered -> {
                with(uiState.value) { setState(copy(password = password.copy(value = newEvent.password))) }
            }

            is SignUpContract.Event.OnPasswordCheckEntered -> {
                with(uiState.value) { setState(copy(passwordCheck = passwordCheck.copy(value = newEvent.passwordCheck))) }
            }

            is SignUpContract.Event.OnNicknameEntered -> {
                with(uiState.value) { setState(copy(nickname = nickname.copy(value = newEvent.nickname))) }
            }

            is SignUpContract.Event.OnNameEntered -> {
                with(uiState.value) { setState(copy(name = name.copy(value = newEvent.name))) }
            }

            is SignUpContract.Event.OnBirthdayEntered -> {
                var birthdayNum = numberRegex.replace(newEvent.birthday, "")
                birthdayNum = if (birthdayNum.length > 8) birthdayNum.substring(0..7) else birthdayNum
                with(uiState.value) { setState(copy(birthday = birthday.copy(value = birthdayNum))) }
            }

            is SignUpContract.Event.OnPhoneEntered -> {
                var phoneNum = numberRegex.replace(newEvent.phone, "")
                phoneNum = if (phoneNum.length > 11) phoneNum.substring(0..10) else phoneNum
                with(uiState.value) { setState(copy(phone = phone.copy(value = phoneNum))) }
            }

            is SignUpContract.Event.OnNextClicked -> {
                if (checkFirstInfo()) {
                    setEffect { SignUpContract.Effect.ScrollToNextPage }
                }
            }

            is SignUpContract.Event.OnAcceptClicked -> {
                if (checkSecondInfo()) {
                    setState(uiState.value.copy(isLoading = true))
                    viewModelScope.launch {
                        requestSignUp()
                    }
                }
            }

            is SignUpContract.Event.OnBack -> {
                setEffect { SignUpContract.Effect.ScrollToPreviousPage }
            }

            is SignUpContract.Event.OnError -> {
                setEffect { SignUpContract.Effect.ShowSnackbar(newEvent.errorMessage) }
            }
        }
    }

    private fun requestSignUp() {
        viewModelScope.launch {
            signUpRepository.requestSignUp(uiState.value.mapToUser())
                .onSuccess {
                    Log.d("SignUpSuccess", uiState.value.mapToUser().toString()) // TODO: 테스트 후 삭제
                    setState(uiState.value.copy(isLoading = false, isSuccess = true))
                    setEffect { SignUpContract.Effect.NavigateToLogin }
                }
                .onFailure {
                    Log.e("SignUpFailure", "SignUp Failure", it) // TODO: 테스트 후 삭제
                    setState(uiState.value.copy(isLoading = false, isError = true))
                    setEvent(SignUpContract.Event.OnError(""))
                }
        }
    }

    private fun checkEmail(email: String) {
        viewModelScope.launch {
            signUpRepository.checkEmail(email)
                .onSuccess {
                    // TODO: 중복 확인 성공, 실패 메시지 출력
                }
                .onFailure {
                    Log.e("SignUpFailure", "Check Email Failure", it) // TODO: 테스트 후 삭제
                    setState(uiState.value.copy(isLoading = false, isError = true))
                    setEvent(SignUpContract.Event.OnError(""))
                }
        }
    }

    private fun checkNickname(nickname: String) {
        viewModelScope.launch {
            signUpRepository.checkNickname(nickname)
                .onSuccess { result ->
                    // TODO: 중복 확인 성공, 실패 메시지 출력
                }
                .onFailure {
                    Log.e("SignUpFailure", "Check Nickname Failure", it) // TODO: 테스트 후 삭제
                    setState(uiState.value.copy(isLoading = false, isError = true))
                    setEvent(SignUpContract.Event.OnError(""))
                }
        }
    }

    private fun checkFirstInfo(): Boolean {
        return checkEmail() && checkPassword() && checkPasswordCheck()
    }

    private fun checkSecondInfo(): Boolean {
        return checkNickname() && checkName() && checkBirthday() && checkPhone()
    }

    private fun checkEmail(): Boolean {
        return with(uiState.value) {
            when {
                email.value.isEmpty() -> {
                    setState(copy(email = email.copy(isError = true, errorMessage = "이메일 입력을 확인하세요")))
                    setEvent(SignUpContract.Event.OnError("이메일 입력을 확인하세요"))
                    false
                }

                emailRegex.matches(email.value).not() -> {
                    setState(copy(email = email.copy(isError = true, errorMessage = "이메일 형식이 올바르지 않습니다")))
                    setEvent(SignUpContract.Event.OnError("이메일 형식이 올바르지 않습니다"))
                    false
                }

                else -> {
                    setState(copy(email = email.copy(isError = false)))
                    true
                }
            }
        }
    }

    private fun checkPassword(): Boolean {
        return with(uiState.value) {
            when {
                password.value.isEmpty() -> {
                    setState(copy(password = password.copy(isError = true, errorMessage = "비밀번호 입력을 확인하세요")))
                    setEvent(SignUpContract.Event.OnError("비밀번호 입력을 확인하세요"))
                    false
                }

                passwordRegex.matches(password.value).not() -> {
                    setState(copy(password = password.copy(isError = true, errorMessage = "비밀번호 형식이 올바르지 않습니다")))
                    setEvent(SignUpContract.Event.OnError("비밀번호 형식이 올바르지 않습니다"))
                    false
                }

                else -> {
                    setState(copy(password = password.copy(isError = false)))
                    true
                }
            }
        }
    }

    private fun checkPasswordCheck(): Boolean {
        return with(uiState.value) {
            when {
                passwordCheck.value != password.value -> {
                    setState(copy(passwordCheck = passwordCheck.copy(isError = true, errorMessage = "비밀번호가 일치하지 않습니다")))
                    setEvent(SignUpContract.Event.OnError("비밀번호가 일치하지 않습니다"))
                    false
                }

                else -> {
                    setState(copy(passwordCheck = passwordCheck.copy(isError = false)))
                    true
                }
            }
        }
    }

    private fun checkNickname(): Boolean {
        return with(uiState.value) {
            if (nickname.value.isEmpty()) {
                setState(copy(nickname = nickname.copy(isError = true, errorMessage = "별명 입력을 확인하세요")))
                setEvent(SignUpContract.Event.OnError("별명 입력을 확인하세요"))
                false
            } else {
                setState(copy(nickname = nickname.copy(isError = false)))
                true
            }
        }
    }

    private fun checkName(): Boolean {
        return with(uiState.value) {
            when {
                name.value.isEmpty() -> {
                    setState(copy(name = name.copy(isError = true, errorMessage = "이름 입력을 확인하세요")))
                    setEvent(SignUpContract.Event.OnError("이름 입력을 확인하세요"))
                    false
                }

                else -> {
                    setState(copy(name = name.copy(isError = false)))
                    true
                }
            }
        }
    }

    private fun checkBirthday(): Boolean {
        return with(uiState.value) {
            when {
                birthday.value.isEmpty() || birthday.value.length < 8 -> {
                    setState(copy(birthday = birthday.copy(isError = true, errorMessage = "생년월일 입력을 확인하세요")))
                    setEvent(SignUpContract.Event.OnError("생년월일 입력을 확인하세요"))
                    false
                }

                else -> {
                    setState(copy(birthday = birthday.copy(isError = false)))
                    true
                }
            }
        }
    }

    private fun checkPhone(): Boolean {
        return with(uiState.value) {
            when {
                phone.value.isEmpty() || phone.value.length < 11 -> {
                    setState(copy(phone = phone.copy(isError = true, errorMessage = "전화번호 입력을 확인하세요")))
                    setEvent(SignUpContract.Event.OnError("전화번호 입력을 확인하세요"))
                    false
                }

                else -> {
                    setState(copy(phone = phone.copy(isError = false)))
                    true
                }
            }
        }
    }
}
