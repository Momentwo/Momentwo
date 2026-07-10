package cord.eoeo.momentwo.data.login

import cord.eoeo.momentwo.data.model.LoginRequest
import cord.eoeo.momentwo.domain.login.LoginRepository
import cord.eoeo.momentwo.domain.mapper.ProfileMapper
import cord.eoeo.momentwo.core.model.LoginData

class LoginRepositoryImpl(
    private val loginRemoteDataSource: LoginDataSource,
    private val profileMapper: ProfileMapper,
) : LoginRepository {
    override suspend fun requestLogin(
        email: String,
        password: String,
    ): Result<LoginData> =
        runCatching {
            val loginResponse = loginRemoteDataSource.requestLogin(LoginRequest(email, password))

            val accessToken = loginResponse.headers()["Authorization"] ?: throw Exception("Access Token cannot be null")
            val refreshToken = loginResponse.headers()["Refresh"] ?: throw Exception("Refresh Token cannot be null")
            val userProfile = loginResponse.body() ?: throw Exception("Body cannot be null")

            LoginData(accessToken, refreshToken, profileMapper.dataToDomain(userProfile))
        }
}
