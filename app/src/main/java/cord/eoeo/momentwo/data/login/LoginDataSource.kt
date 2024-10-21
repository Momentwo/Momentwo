package cord.eoeo.momentwo.data.login

import cord.eoeo.momentwo.data.model.LoginRequest
import retrofit2.Response

interface LoginDataSource {
    suspend fun requestLogin(loginData: LoginRequest): Response<Unit>
}
