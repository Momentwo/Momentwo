package cord.eoeo.momentwo.feature.auth.impl.login

import android.util.Log
import cord.eoeo.momentwo.core.common.ReducerViewModel
import cord.eoeo.momentwo.core.common.RequestKey
import cord.eoeo.momentwo.core.common.RequestPolicy
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.domain.login.RequestLoginUseCase
import cord.eoeo.momentwo.core.domain.login.TryAutoLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    private val requestLoginUseCase: RequestLoginUseCase,
    private val tryAutoLoginUseCase: TryAutoLoginUseCase,
) : ReducerViewModel<LoginState, LoginEvent, LoginAction, LoginEffect>(LoginState(), LoginReducer) {
    private data object Authentication : RequestKey
    private var initialized = false
    private var manualLoginAttempted = false

    fun initialize() {
        if (initialized) return
        initialized = true
        if (!manualLoginAttempted) authenticate(automatic = true)
    }

    override fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailEntered -> dispatch(LoginAction.EmailChanged(event.email))
            is LoginEvent.OnPasswordEntered -> dispatch(LoginAction.PasswordChanged(event.password))
            LoginEvent.OnLoginClicked -> {
                if (!uiState.value.isSubmittingCredentials) {
                    manualLoginAttempted = true
                    requests.cancel(Authentication)
                    authenticate(automatic = false)
                }
            }
            LoginEvent.OnRequestAutoLogin -> initialize()
            LoginEvent.OnSignUpClicked -> {
                requests.cancel(Authentication)
                sendEffect(LoginEffect.NavigateToSignUp)
            }
            is LoginEvent.OnError -> sendEffect(LoginEffect.ShowSnackbar(event.errorMessage))
        }
    }

    private fun authenticate(automatic: Boolean) {
        val email = uiState.value.email
        val password = uiState.value.password
        requests.launchRequest(
            key = Authentication,
            policy = RequestPolicy.IgnoreWhileRunning,
            request = { if (automatic) tryAutoLoginUseCase() else requestLoginUseCase(email, password) },
            onStarted = { dispatch(LoginAction.Started(automatic)) },
            onSuccess = {
                dispatch(LoginAction.Succeeded)
                emitEffect(LoginEffect.NavigateToAlbum)
            },
            onFailure = { exception ->
                Log.e("Login", "Login request failed", exception)
                if (!automatic) {
                    dispatch(LoginAction.Failed(exception.toUiError()))
                    emitEffect(LoginEffect.ShowSnackbar("로그인 요청에 실패했습니다"))
                }
            },
            onFinished = { dispatch(LoginAction.Finished) },
        )
    }
}
