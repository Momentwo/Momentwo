package cord.eoeo.momentwo.core.datastore

import cord.eoeo.momentwo.core.common.runSuspendCatching

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.distinctUntilChanged

class PreferenceRepositoryImpl(
    private val userDataStore: DataStore<Preferences>,
) : PreferenceRepository {
    private val account = androidx.datastore.preferences.core.stringPreferencesKey("session.account")
    private val epoch = androidx.datastore.preferences.core.stringPreferencesKey("session.epoch")
    private val expired = androidx.datastore.preferences.core.booleanPreferencesKey("session.expired")
    private fun identity(values: Preferences): cord.eoeo.momentwo.core.model.AccountIdentity? {
        val id = values[account]?.takeIf { it.isNotBlank() } ?: return null
        val generation = values[epoch] ?: return null
        return cord.eoeo.momentwo.core.model.AccountIdentity(id,generation,values[expired] != true && !values[PreferenceKeys.ACCESS_TOKEN_KEY].isNullOrBlank())
    }
    override fun identities() = userDataStore.data.map { identity(it) }.distinctUntilChanged()
    override suspend fun getSession(): Result<StoredSession?> = runSuspendCatching {
        val values = userDataStore.data.first()
        identity(values)?.let { StoredSession(it,values[PreferenceKeys.ACCESS_TOKEN_KEY].orEmpty(),values[PreferenceKeys.REFRESH_TOKEN_KEY].orEmpty()) }
    }
    override suspend fun storeSession(accountId: String, accessToken: String, refreshToken: String) {
        require(accountId.isNotBlank() && accessToken.isNotBlank() && refreshToken.isNotBlank())
        val generation = java.util.UUID.randomUUID().toString()
        userDataStore.edit { it[account]=accountId; it[epoch]=generation; it[expired]=false; it[PreferenceKeys.ACCESS_TOKEN_KEY]=accessToken; it[PreferenceKeys.REFRESH_TOKEN_KEY]=refreshToken }
    }
    override suspend fun updateSessionTokens(session: StoredSession, accessToken: String, refreshToken: String): Boolean {
        require(accessToken.isNotBlank() && refreshToken.isNotBlank())
        var stored = false
        userDataStore.edit { if(it[account] == session.identity.accountId && it[epoch] == session.identity.epoch && it[PreferenceKeys.ACCESS_TOKEN_KEY] == session.accessToken) { it[PreferenceKeys.ACCESS_TOKEN_KEY]=accessToken;it[PreferenceKeys.REFRESH_TOKEN_KEY]=refreshToken;it[expired]=false;stored=true } }
        return stored
    }
    override suspend fun clearSession() { userDataStore.edit { it.remove(account); it.remove(epoch); it.remove(expired); it.remove(PreferenceKeys.ACCESS_TOKEN_KEY); it.remove(PreferenceKeys.REFRESH_TOKEN_KEY) } }
    override suspend fun expireSession(accountId: String, epoch: String) { userDataStore.edit { if(it[account]==accountId && it[this.epoch]==epoch) it[expired]=true } }

    override suspend fun storeAccessToken(accessToken: String) {
        storeStringData(PreferenceKeys.ACCESS_TOKEN_KEY, accessToken)
    }

    override suspend fun storeRefreshToken(refreshToken: String) {
        storeStringData(PreferenceKeys.REFRESH_TOKEN_KEY, refreshToken)
    }

    override suspend fun getAccessToken(): Result<String> = getStringData(PreferenceKeys.ACCESS_TOKEN_KEY)

    override suspend fun getRefreshToken(): Result<String> = getStringData(PreferenceKeys.REFRESH_TOKEN_KEY)

    private suspend fun storeStringData(
        key: Preferences.Key<String>,
        string: String,
    ) {
        userDataStore.edit { preferences ->
            preferences[key] = string
        }
    }

    private suspend fun getStringData(key: Preferences.Key<String>): Result<String> =
        runSuspendCatching {
            userDataStore.data
                .catch { exception ->
                    when (exception) {
                        is IOException -> emit(emptyPreferences())
                        else -> throw exception
                    }
                }.map { preferences ->
                    preferences[key]
                }.firstOrNull()
                .orEmpty()
        }
}
