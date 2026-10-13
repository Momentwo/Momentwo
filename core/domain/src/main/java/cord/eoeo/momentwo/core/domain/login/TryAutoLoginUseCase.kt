package cord.eoeo.momentwo.core.domain.login

import cord.eoeo.momentwo.core.common.runSuspendCatching
import javax.inject.Inject

import cord.eoeo.momentwo.core.datastore.PreferenceRepository

class TryAutoLoginUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
) {
    suspend operator fun invoke(): Result<Unit> =
        runSuspendCatching {
            val accessToken = preferenceRepository.getAccessToken().getOrThrow()

            if (accessToken.isEmpty()) throw Exception("AutoLogin: No login history")

            Unit
        }
}
