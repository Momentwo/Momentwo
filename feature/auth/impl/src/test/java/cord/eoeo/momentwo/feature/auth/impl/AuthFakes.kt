package cord.eoeo.momentwo.feature.auth.impl

import cord.eoeo.momentwo.core.data.login.LoginRepository
import cord.eoeo.momentwo.core.data.model.User
import cord.eoeo.momentwo.core.data.profile.ProfileRepository
import cord.eoeo.momentwo.core.data.signup.SignUpRepository
import cord.eoeo.momentwo.core.datastore.PreferenceRepository
import cord.eoeo.momentwo.core.model.LoginData
import cord.eoeo.momentwo.core.model.Profile

internal val fixtureProfile = Profile("Fixture", "fixture@example.com", "fixture", "01000000000", "")

internal class FakeLoginRepository : LoginRepository {
    var result = Result.success(LoginData("fixture-access", "fixture-refresh", fixtureProfile))
    var response: (suspend () -> Result<LoginData>)? = null
    val requests = mutableListOf<Pair<String, String>>()
    override suspend fun requestLogin(email: String, password: String): Result<LoginData> {
        requests += email to password
        return response?.invoke() ?: result
    }
}

internal class FakePreferenceRepository : PreferenceRepository {
    var accessToken = ""
    var accessReads = 0
    var accessResponse: (suspend () -> Result<String>)? = null
    var refreshToken = ""
    override suspend fun storeAccessToken(accessToken: String) { this.accessToken = accessToken }
    override suspend fun storeRefreshToken(refreshToken: String) { this.refreshToken = refreshToken }
    override suspend fun getAccessToken(): Result<String> {
        accessReads++
        return accessResponse?.invoke() ?: Result.success(accessToken)
    }
    override suspend fun getRefreshToken() = Result.success(refreshToken)
}

internal class FakeProfileRepository : ProfileRepository {
    var storedProfile: Profile? = null
    override suspend fun storeProfile(profile: Profile): Result<Unit> {
        storedProfile = profile
        return Result.success(Unit)
    }
    override suspend fun getProfile(nickname: String?) = Result.success(fixtureProfile)
}

internal class FakeSignUpRepository : SignUpRepository {
    val requests = mutableListOf<User>()
    override suspend fun requestSignUp(user: User): Result<Unit> {
        requests += user
        return Result.success(Unit)
    }
    override suspend fun checkEmail(email: String) = Result.success(Unit)
    override suspend fun checkNickname(nickname: String) = Result.success(Unit)
}
