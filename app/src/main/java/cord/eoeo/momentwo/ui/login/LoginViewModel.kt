package cord.eoeo.momentwo.ui.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.core.domain.login.RequestLoginUseCase
import cord.eoeo.momentwo.core.domain.login.TryAutoLoginUseCase
import cord.eoeo.momentwo.core.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val requestLoginUseCase: RequestLoginUseCase,
        private val tryAutoLoginUseCase: TryAutoLoginUseCase,
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
                            requestLoginUseCase(email, password)
                                .onSuccess {
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
                            tryAutoLoginUseCase()
                                .onSuccess {
                                    setState(copy(isLoading = false, isSuccess = true, isError = false))
                                    setEffect { LoginContract.Effect.NavigateToAlbum }
                                }.onFailure { exception ->
                                    Log.e("Login", "AutoLogin Failure", exception)
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
