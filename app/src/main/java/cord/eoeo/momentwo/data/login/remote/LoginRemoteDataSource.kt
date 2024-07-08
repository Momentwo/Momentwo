package cord.eoeo.momentwo.data.login.remote

import cord.eoeo.momentwo.data.login.LoginDataSource
import cord.eoeo.momentwo.data.model.LoginRequest
import cord.eoeo.momentwo.data.model.LoginResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginRemoteDataSource(
    private val loginService: LoginService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LoginDataSource {
    override suspend fun requestLogin(loginData: LoginRequest): Result<LoginResponse> = runCatching {
        withContext(dispatcher) {
            loginService.postLogin(loginData)
        }
    }
}
