package cord.eoeo.momentwo.data.authentication

import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    private const val ACCESS_TOKEN = "access_token"
    private const val REFRESH_TOKEN = "refresh_token"

    val ACCESS_TOKEN_KEY = stringPreferencesKey(ACCESS_TOKEN)
    val REFRESH_TOKEN_KEY = stringPreferencesKey(REFRESH_TOKEN)
}
