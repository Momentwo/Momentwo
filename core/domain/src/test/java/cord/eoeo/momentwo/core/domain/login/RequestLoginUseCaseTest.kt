package cord.eoeo.momentwo.core.domain.login

import cord.eoeo.momentwo.core.data.login.LoginRepository
import cord.eoeo.momentwo.core.data.profile.ProfileRepository
import cord.eoeo.momentwo.core.datastore.PreferenceRepository
import cord.eoeo.momentwo.core.model.LoginData
import cord.eoeo.momentwo.core.model.Profile
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RequestLoginUseCaseTest {
    private val profile = Profile("name", "mail", "nickname", "phone", "image")
    private val login = object : LoginRepository {
        override suspend fun requestLogin(email: String, password: String) =
            Result.success(LoginData("access", "refresh", profile))
    }

    @Test fun cancelledTokenWriteCannotContinueSessionWrites() = runTest {
        val writes = mutableListOf<String>()
        val preferences = object : PreferenceRepository {
            override suspend fun storeAccessToken(accessToken: String) {
                writes += "access"
                currentCoroutineContext().cancel()
                // A dependency may return even after its job is cancelled.
            }
            override suspend fun storeRefreshToken(refreshToken: String) { writes += "refresh" }
            override suspend fun getAccessToken() = Result.success("")
            override suspend fun getRefreshToken() = Result.success("")
        }
        val profiles = object : ProfileRepository {
            override suspend fun storeProfile(profile: Profile): Result<Unit> {
                writes += "profile"
                return Result.success(Unit)
            }
            override suspend fun getProfile(nickname: String?) = Result.success(profile)
        }
        var completed = false
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            RequestLoginUseCase(login, profiles, preferences)("email", "password")
                .onSuccess { completed = true }
        }
        job.join()
        assertEquals(listOf("access"), writes)
        assertFalse(completed)
    }

    @Test fun profileStorageFailureIsNotLoginSuccess() = runTest {
        val failure = IOException("profile storage failed")
        val preferences = object : PreferenceRepository {
            override suspend fun storeAccessToken(accessToken: String) = Unit
            override suspend fun storeRefreshToken(refreshToken: String) = Unit
            override suspend fun getAccessToken() = Result.success("")
            override suspend fun getRefreshToken() = Result.success("")
        }
        val profiles = object : ProfileRepository {
            override suspend fun storeProfile(profile: Profile) = Result.failure<Unit>(failure)
            override suspend fun getProfile(nickname: String?) = Result.success(profile)
        }
        assertSame(failure, RequestLoginUseCase(login, profiles, preferences)("email", "password").exceptionOrNull())
    }
}
