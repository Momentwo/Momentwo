package cord.eoeo.momentwo.core.data.login.remote

import cord.eoeo.momentwo.core.data.login.LoginDataSource
import cord.eoeo.momentwo.core.data.model.LoginRequest
import cord.eoeo.momentwo.core.data.model.UserProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Response

class LoginRemoteDataSource(
    private val loginService: LoginService,
    private val dispatcher: CoroutineDispatcher,
) : LoginDataSource {
    override suspend fun requestLogin(loginData: LoginRequest): Response<UserProfile> =
        withContext(dispatcher) {
            loginService.postLogin(loginData)
        }
}
