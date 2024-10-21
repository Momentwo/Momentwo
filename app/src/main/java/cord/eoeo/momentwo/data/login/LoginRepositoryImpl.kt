package cord.eoeo.momentwo.data.login

import android.util.Log
import cord.eoeo.momentwo.data.model.LoginRequest
import cord.eoeo.momentwo.ui.model.TokenItem

class LoginRepositoryImpl(
    private val loginRemoteDataSource: LoginDataSource
) : LoginRepository {
    override suspend fun requestLogin(email: String, password: String): Result<TokenItem> = runCatching {
        val loginResponse = loginRemoteDataSource.requestLogin(LoginRequest(email, password))

        val accessToken = loginResponse.headers()["Authorization"] ?: ""
        val refreshToken = loginResponse.headers()["Refresh"] ?: ""

        Log.d("Login", "accessToken: $accessToken")
        Log.d("Login", "refreshToken: $refreshToken")

        TokenItem(accessToken, refreshToken)
    }
}
