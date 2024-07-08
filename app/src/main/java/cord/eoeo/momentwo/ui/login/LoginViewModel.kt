package cord.eoeo.momentwo.ui.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.data.login.LoginRepository
import cord.eoeo.momentwo.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : BaseViewModel<LoginContract.State, LoginContract.Event, LoginContract.Effect>() {
    override fun createInitialState(): LoginContract.State = LoginContract.State()

    override fun handleEvent(newEvent: LoginContract.Event) {
        when (newEvent) {
            is LoginContract.Event.OnEmailEntered -> {
                with(uiState.value) { setState(copy(email = newEvent.email)) }
            }

            is LoginContract.Event.OnPasswordEntered -> {
                with(uiState.value) { setState(copy(password = newEvent.password)) }
            }

            is LoginContract.Event.OnLoginClicked -> {
                viewModelScope.launch {
                    with(uiState.value) {
                        setState(copy(isLoading = true))
                        loginRepository.requestLogin(email, password)
                            .onSuccess {
                                Log.d("LoginSuccess", it.toString())
                                setState(copy(isLoading = false, isSuccess = true, isError = false))
                                setEffect { LoginContract.Effect.NavigateToAlbum }
                            }
                            .onFailure {
                                Log.e("LoginFailure", "Login Failure", it)
                                setState(copy(isLoading = false, isSuccess = false, isError = true))
                                setEvent(LoginContract.Event.OnError("로그인 요청에 실패했습니다"))
                            }
                    }
                }
            }

            is LoginContract.Event.OnSignUpClicked -> {
                setEffect { LoginContract.Effect.NavigateToSignUp }
            }

            is LoginContract.Event.OnError -> {
                setEffect { LoginContract.Effect.ShowSnackbar(newEvent.errorMessage) }
            }
        }
    }
}
