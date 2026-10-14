package cord.eoeo.momentwo.core.data.library
import cord.eoeo.momentwo.core.datastore.*
import cord.eoeo.momentwo.core.network.ApiRequestCounter
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.*
import retrofit2.Retrofit
/** A request is bound to a verified account epoch and captured credential; writes are never replayed by OkHttp. */
class AccountClientFactory @Inject constructor(private val preferences: PreferenceRepository, private val retrofit: Retrofit, private val counter: ApiRequestCounter) {
    suspend fun snapshot(): StoredSession = requireNotNull(preferences.getSession().getOrThrow()) { "계정을 연결해주세요" }
    suspend fun check(session: StoredSession, requireConnected: Boolean = true) {
        val stored = preferences.getSession().getOrThrow()
        val now = stored?.identity
        if(now?.accountId != session.identity.accountId || now.epoch != session.identity.epoch || (requireConnected && (!now.canSync || stored.accessToken != session.accessToken))) throw IOException("계정 연결이 변경되었거나 만료되었습니다")
    }
    fun client(session: StoredSession): Retrofit {
        val base = retrofit.baseUrl()
        val transport = (retrofit.callFactory() as OkHttpClient).newBuilder().apply { interceptors().clear(); networkInterceptors().clear() }
            .authenticator(Authenticator.NONE).retryOnConnectionFailure(false).followRedirects(false).followSslRedirects(false)
            .addInterceptor { chain -> runBlocking {
                check(session)
                val request = chain.request()
                val api = request.url.host == base.host && request.url.port == base.port && request.url.scheme == base.scheme
                if(api) counter.record()
                val response = chain.proceed(if(api) request.newBuilder().header("Authorization","Bearer ${session.accessToken}").build() else request.newBuilder().removeHeader("Authorization").removeHeader("Refresh").build())
                try {
                    check(session)
                    if(api && response.code == 401) preferences.expireSession(session.identity.accountId,session.identity.epoch)
                    response
                } catch(failure: Throwable) { response.close(); throw failure }
            } }.build()
        return retrofit.newBuilder().client(transport).build()
    }
}
