package cord.eoeo.momentwo.core.datastore

class StoredSession(val identity: cord.eoeo.momentwo.core.model.AccountIdentity, val accessToken: String, val refreshToken: String) {
    override fun toString() = "StoredSession(credentials=redacted)"
}
interface PreferenceRepository {
    fun identities(): kotlinx.coroutines.flow.Flow<cord.eoeo.momentwo.core.model.AccountIdentity?>
    suspend fun getSession(): Result<StoredSession?>
    suspend fun storeSession(accountId: String, accessToken: String, refreshToken: String)
    suspend fun updateSessionTokens(session: StoredSession, accessToken: String, refreshToken: String): Boolean
    suspend fun clearSession()
    suspend fun expireSession(accountId: String, epoch: String)

    suspend fun storeAccessToken(accessToken: String)

    suspend fun storeRefreshToken(refreshToken: String)

    suspend fun getAccessToken(): Result<String>

    suspend fun getRefreshToken(): Result<String>
}
