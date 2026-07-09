package cord.eoeo.momentwo.core.data.login

import cord.eoeo.momentwo.core.model.LoginData

interface LoginRepository {
    suspend fun requestLogin(
        email: String,
        password: String,
    ): Result<LoginData>
}
