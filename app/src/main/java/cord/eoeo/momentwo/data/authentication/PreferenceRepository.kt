package cord.eoeo.momentwo.data.authentication

interface PreferenceRepository {
    suspend fun storeAccessToken(accessToken: String)

    suspend fun storeRefreshToken(refreshToken: String)

    suspend fun getAccessToken(): Result<String>

    suspend fun getRefreshToken(): Result<String>
}
