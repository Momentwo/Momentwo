package cord.eoeo.momentwo.core.data.profile.local

import cord.eoeo.momentwo.core.common.runSuspendCatching

import cord.eoeo.momentwo.core.data.profile.ProfileDataSource
import cord.eoeo.momentwo.core.database.ProfileDao
import cord.eoeo.momentwo.core.database.ProfileEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ProfileLocalDataSource(
    private val profileDao: ProfileDao,
    private val dispatcher: CoroutineDispatcher,
    private val preferences: cord.eoeo.momentwo.core.datastore.PreferenceRepository,
) : ProfileDataSource.Local {
    override suspend fun storeProfile(profile: ProfileEntity): Result<Unit> =
        runSuspendCatching {
            withContext(dispatcher) {
                profileDao.insertUser(profile)
            }
        }

    override suspend fun getProfile(): Result<ProfileEntity> =
        runSuspendCatching {
            withContext(dispatcher) {
                val owner = requireNotNull(preferences.getSession().getOrThrow()).identity.accountId
                requireNotNull(profileDao.getProfile(owner))
            }
        }
}
