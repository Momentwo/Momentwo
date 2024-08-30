package cord.eoeo.momentwo.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class PreferenceRepositoryImpl(
    private val userDataStore: DataStore<Preferences>,
) : PreferenceRepository {
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
        Result.runCatching {
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
