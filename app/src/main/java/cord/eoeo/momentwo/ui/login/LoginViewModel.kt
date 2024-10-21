package cord.eoeo.momentwo.ui.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.data.authentication.PreferenceRepository
import cord.eoeo.momentwo.data.login.LoginRepository
import cord.eoeo.momentwo.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val preferenceRepository: PreferenceRepository,
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
                        loginRepository
                            .requestLogin(email, password)
                            .onSuccess { tokenItem ->
                                preferenceRepository.storeAccessToken(tokenItem.accessToken)
                                preferenceRepository.storeRefreshToken(tokenItem.refreshToken)
                                setState(copy(isLoading = false, isSuccess = true, isError = false))
                                setEffect { LoginContract.Effect.NavigateToAlbum }
                            }.onFailure { exception ->
                                Log.e("LoginFailure", "Login Failure", exception)
                                setState(copy(isLoading = false, isSuccess = false, isError = true))
                                setEvent(LoginContract.Event.OnError("로그인 요청에 실패했습니다"))
                            }
                    }
                }
            }

            is LoginContract.Event.OnSignUpClicked -> {
                setEffect { LoginContract.Effect.NavigateToSignUp }
            }

            is LoginContract.Event.OnRequestAutoLogin -> {
                viewModelScope.launch {
                    with(uiState.value) {
                        setState(copy(isLoading = true))
                        preferenceRepository
                            .getAccessToken()
                            .onSuccess { accessToken ->
                                if (accessToken.isNotEmpty()) {
                                    Log.d("Login", "AutoLogin: $accessToken")
                                    setState(copy(isLoading = false, isSuccess = true, isError = false))
                                    setEffect { LoginContract.Effect.NavigateToAlbum }
                                } else {
                                    Log.d("Login", "AutoLogin: No login history")
                                    setState(copy(isLoading = false))
                                }
                            }.onFailure {
                                Log.e("Login", "AutoLogin Failure", it)
                                setState(copy(isLoading = false))
                            }
                    }
                }
            }

            is LoginContract.Event.OnError -> {
                setEffect { LoginContract.Effect.ShowSnackbar(newEvent.errorMessage) }
            }
        }
    }
}
