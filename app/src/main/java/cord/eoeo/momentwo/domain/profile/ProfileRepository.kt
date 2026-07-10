package cord.eoeo.momentwo.domain.profile

import cord.eoeo.momentwo.core.model.Profile

interface ProfileRepository {
    suspend fun storeProfile(profile: Profile): Result<Unit>

    suspend fun getProfile(nickname: String?): Result<Profile>
}
