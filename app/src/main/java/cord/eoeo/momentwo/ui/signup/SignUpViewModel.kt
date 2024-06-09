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
    override fun createInitialState(): SignUpContract.State = SignUpContract.State()

    override fun handleEvent(newEvent: SignUpContract.Event) {
        when (newEvent) {
            is SignUpContract.Event.OnEmailEntered -> {
                setState(uiState.value.copy(email = newEvent.email))
            }

            is SignUpContract.Event.OnPasswordEntered -> {
                setState(uiState.value.copy(password = newEvent.password))
            }

            is SignUpContract.Event.OnPasswordCheckEntered -> {
                setState(uiState.value.copy(passwordCheck = newEvent.password))
            }

            is SignUpContract.Event.OnBirthdayEntered -> {
                setState(uiState.value.copy(birthday = newEvent.birthday))
            }

            is SignUpContract.Event.OnNameEntered -> {
                setState(uiState.value.copy(name = newEvent.name))
            }

            is SignUpContract.Event.OnNicknameEntered -> {
                setState(uiState.value.copy(nickname = newEvent.nickname))
            }

            is SignUpContract.Event.OnPhone1Entered -> {
                setState(uiState.value.copy(phone1 = newEvent.phone1))
            }

            is SignUpContract.Event.OnPhone2Entered -> {
                setState(uiState.value.copy(phone2 = newEvent.phone2))
            }

            is SignUpContract.Event.OnPhone3Entered -> {
                setState(uiState.value.copy(phone3 = newEvent.phone3))
            }

            is SignUpContract.Event.OnAddress1Entered -> {
                setState(uiState.value.copy(address1 = newEvent.address1))
            }

            is SignUpContract.Event.OnAddress2Entered -> {
                setState(uiState.value.copy(address2 = newEvent.address2))
            }

            SignUpContract.Event.OnAcceptClicked -> {
                setState(uiState.value.copy(isLoading = true))
                viewModelScope.launch {
                    signUpRepository.requestSignUp(uiState.value.mapToUser())
                        .onSuccess {
                            Log.d("SignUpSuccess", uiState.value.mapToUser().toString())
                            setState(uiState.value.copy(isLoading = false))
                            setEffect { SignUpContract.Effect.NavigateToLogin }
                        }
                        .onFailure {
                            Log.e("SignUpFailure", "SignUp Failure", it)
                            setState(uiState.value.copy(isLoading = false, isError = true))
                        }
                }
            }
        }
    }
}
