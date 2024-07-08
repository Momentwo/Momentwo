package cord.eoeo.momentwo.data.login

import cord.eoeo.momentwo.data.model.LoginRequest
import cord.eoeo.momentwo.data.model.LoginResponse

class LoginRepositoryImpl(
    private val loginRemoteDataSource: LoginDataSource
) : LoginRepository {
    override suspend fun requestLogin(email: String, password: String): Result<LoginResponse> {
        return loginRemoteDataSource.requestLogin(LoginRequest(email, password))
    }
}
