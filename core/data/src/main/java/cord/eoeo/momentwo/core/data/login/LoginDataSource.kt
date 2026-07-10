package cord.eoeo.momentwo.core.data.login

import cord.eoeo.momentwo.core.data.model.LoginRequest
import cord.eoeo.momentwo.core.data.model.UserProfile
import retrofit2.Response

interface LoginDataSource {
    suspend fun requestLogin(loginData: LoginRequest): Response<UserProfile>
}
