package cord.eoeo.momentwo.core.data.mapper

import cord.eoeo.momentwo.core.data.model.UserProfile
import cord.eoeo.momentwo.core.database.ProfileEntity
import cord.eoeo.momentwo.core.model.Profile
import cord.eoeo.momentwo.core.model.ProfileItem
import javax.inject.Inject

class ProfileMapper
    @Inject
    constructor() {
        fun entityToDomain(profileEntity: ProfileEntity): Profile =
            Profile(
                name = profileEntity.name,
                email = profileEntity.username,
                nickname = profileEntity.nickname,
                phone = profileEntity.phone,
                profileImage = profileEntity.userProfileImage,
            )

        fun domainToEntity(profile: Profile): ProfileEntity =
            ProfileEntity(
                username = profile.email,
                name = profile.name,
                nickname = profile.nickname,
                phone = profile.phone,
                userProfileImage = profile.profileImage,
            )

        fun dataToDomain(userProfile: UserProfile): Profile =
            Profile(
                name = userProfile.name,
                email = userProfile.username,
                nickname = userProfile.nickname,
                phone = userProfile.phone,
                profileImage = userProfile.userProfileImage,
            )

        fun domainToUI(profile: Profile): ProfileItem =
            ProfileItem(
                name = profile.name,
                email = profile.email,
                nickname = profile.nickname,
                phone = profile.phone,
                profileImage = profile.profileImage,
            )
    }
