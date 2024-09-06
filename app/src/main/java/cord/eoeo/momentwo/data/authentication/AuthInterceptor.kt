package cord.eoeo.momentwo.data.authentication

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor
    @Inject
    constructor(
        private val preferenceRepository: PreferenceRepository,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response =
            runBlocking {
                preferenceRepository
                    .getAccessToken()
                    .onSuccess { accessToken ->
                        Log.d("Auth", "Auth Success: $accessToken")
                        return@runBlocking chain.proceed(
                            chain
                                .request()
                                .newBuilder()
                                .addHeader("Authorization", "Bearer $accessToken")
                                .build(),
                        )
                    }.onFailure {
                        Log.e("Auth", "Auth Failure", it)
                        return@runBlocking chain.proceed(chain.request())
                    }
                chain.proceed(chain.request())
            }
    }
