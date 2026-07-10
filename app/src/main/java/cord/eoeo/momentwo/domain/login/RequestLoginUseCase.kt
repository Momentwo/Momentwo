package cord.eoeo.momentwo.domain.login
import cord.eoeo.momentwo.core.data.login.LoginRepository

import cord.eoeo.momentwo.core.datastore.PreferenceRepository
import cord.eoeo.momentwo.core.data.profile.ProfileRepository

class RequestLoginUseCase(
    private val loginRepository: LoginRepository,
    private val profileRepository: ProfileRepository,
    private val preferenceRepository: PreferenceRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): Result<Unit> =
        runCatching {
            val loginData = loginRepository.requestLogin(email, password).getOrThrow()

            preferenceRepository.storeAccessToken(loginData.accessToken)
            preferenceRepository.storeRefreshToken(loginData.refreshToken)
            profileRepository.storeProfile(loginData.profile)
        }
}
