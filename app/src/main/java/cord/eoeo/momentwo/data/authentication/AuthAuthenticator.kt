package cord.eoeo.momentwo.data.authentication

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class AuthAuthenticator @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? =
        runBlocking {
            Log.d("Auth", "AuthAuthenticator")
            if (response.request.headers("Refresh").isNotEmpty()) {
                // response.close()
                return@runBlocking null
            }

            val refreshToken = preferenceRepository.getRefreshToken().getOrDefault("")

            if (refreshToken.isEmpty()) {
                return@runBlocking null
            }

            OkHttpClient().newCall(
                response.request.newBuilder()
                    .addHeader("Refresh", refreshToken)
                    .build()
            ).execute().use { newResponse ->
                if (newResponse.isSuccessful) {
                    val newAccessToken = newResponse.header("Authorization") ?: ""
                    val newRefreshToken = newResponse.header("Refresh") ?: ""

                    Log.d("Auth", "Reissue: Store Reissued Token")
                    Log.d("Auth", "accessToken: $newAccessToken")
                    Log.d("Auth", "refreshToken: $newRefreshToken")

                    CoroutineScope(dispatcher).launch {
                        preferenceRepository.storeAccessToken(newAccessToken)
                        preferenceRepository.storeRefreshToken(newRefreshToken)
                    }
                    return@runBlocking Request.Builder()
                        .url(response.request.url)
                        .addHeader("Authorization", "Bearer $newAccessToken")
                        .build()
                } else {
                    Log.e("Auth", "Reissue Failure ${newResponse.code}")
                }
            }

            // response.close()
            null
        }
}
