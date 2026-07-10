package cord.eoeo.momentwo.core.domain.login
import javax.inject.Inject
import cord.eoeo.momentwo.core.data.login.LoginRepository

import cord.eoeo.momentwo.core.datastore.PreferenceRepository

class TryAutoLoginUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
) {
    suspend operator fun invoke(): Result<Unit> =
        runCatching {
            val accessToken = preferenceRepository.getAccessToken().getOrThrow()

            if (accessToken.isEmpty()) throw Exception("AutoLogin: No login history")

            Result.success(Unit)
        }
}
