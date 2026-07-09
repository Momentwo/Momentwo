package cord.eoeo.momentwo.core.data.profile

import cord.eoeo.momentwo.core.data.mapper.ProfileMapper
import cord.eoeo.momentwo.core.model.Profile
import cord.eoeo.momentwo.core.data.profile.ProfileRepository

class ProfileRepositoryImpl(
    private val profileLocalDataSource: ProfileDataSource.Local,
    private val profileRemoteDataSource: ProfileDataSource.Remote,
    private val profileMapper: ProfileMapper,
) : ProfileRepository {
    override suspend fun storeProfile(profile: Profile): Result<Unit> =
        profileLocalDataSource.storeProfile(profileMapper.domainToEntity(profile))

    override suspend fun getProfile(nickname: String?): Result<Profile> =
        if (nickname.isNullOrEmpty()) {
            profileLocalDataSource.getProfile().map { profileEntity ->
                profileMapper.entityToDomain(profileEntity)
            }
        } else {
            profileRemoteDataSource.getProfile(nickname).map { userProfile ->
                profileMapper.dataToDomain(userProfile)
            }
        }
}
