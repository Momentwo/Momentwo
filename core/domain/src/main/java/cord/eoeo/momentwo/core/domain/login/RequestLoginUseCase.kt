package cord.eoeo.momentwo.core.domain.login

import cord.eoeo.momentwo.core.common.runSuspendCatching
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import javax.inject.Inject
import cord.eoeo.momentwo.core.data.login.LoginRepository

import cord.eoeo.momentwo.core.datastore.PreferenceRepository
import cord.eoeo.momentwo.core.data.profile.ProfileRepository

class RequestLoginUseCase @Inject constructor(
    private val loginRepository: LoginRepository,
    private val profileRepository: ProfileRepository,
    private val preferenceRepository: PreferenceRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): Result<Unit> =
        runSuspendCatching {
            val loginData = loginRepository.requestLogin(email, password).getOrThrow()

            require(loginData.profile.email.isNotBlank()) { "Account identity is missing" }
            // Do not publish a token without its verified canonical owner and stored profile.
            profileRepository.storeProfile(loginData.profile).getOrThrow()
            currentCoroutineContext().ensureActive()
            preferenceRepository.storeSession(loginData.profile.email,loginData.accessToken,loginData.refreshToken)
        }
}
