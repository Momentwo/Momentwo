package cord.eoeo.momentwo.data.login

import cord.eoeo.momentwo.data.model.LoginRequest
import cord.eoeo.momentwo.data.model.LoginResponse

interface LoginDataSource {
    suspend fun requestLogin(loginData: LoginRequest): Result<LoginResponse>
}
