package cord.eoeo.momentwo.data.authentication

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        runBlocking {
            val accessToken = preferenceRepository.getAccessToken().getOrDefault("")

            if (accessToken.isEmpty()) {
                return@runBlocking chain.proceed(chain.request())
            }

            return@runBlocking chain.proceed(
                chain.request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
            )
        }
}
