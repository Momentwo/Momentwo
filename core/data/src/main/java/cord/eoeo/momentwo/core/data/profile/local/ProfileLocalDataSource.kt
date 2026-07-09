package cord.eoeo.momentwo.core.data.profile.local

import cord.eoeo.momentwo.core.data.profile.ProfileDataSource
import cord.eoeo.momentwo.core.database.ProfileDao
import cord.eoeo.momentwo.core.database.ProfileEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileLocalDataSource(
    private val profileDao: ProfileDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ProfileDataSource.Local {
    override suspend fun storeProfile(profile: ProfileEntity): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                profileDao.insertUser(profile)
            }
        }

    override suspend fun getProfile(): Result<ProfileEntity> =
        runCatching {
            withContext(dispatcher) {
                profileDao.getProfile()
            }
        }
}
