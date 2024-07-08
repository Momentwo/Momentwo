package cord.eoeo.momentwo.data.login

import cord.eoeo.momentwo.data.model.LoginResponse

interface LoginRepository {
    suspend fun requestLogin(email: String, password: String): Result<LoginResponse>
}
