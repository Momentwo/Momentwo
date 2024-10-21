package cord.eoeo.momentwo.data.login.remote

import cord.eoeo.momentwo.data.login.LoginDataSource
import cord.eoeo.momentwo.data.model.LoginRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class LoginRemoteDataSource(
    private val loginService: LoginService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LoginDataSource {
    override suspend fun requestLogin(loginData: LoginRequest): Response<Unit> =
        withContext(dispatcher) {
            loginService.postLogin(loginData)
        }
}
