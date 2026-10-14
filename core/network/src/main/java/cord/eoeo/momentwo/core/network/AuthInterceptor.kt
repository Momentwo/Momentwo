package cord.eoeo.momentwo.core.network
import cord.eoeo.momentwo.core.datastore.PreferenceRepository
import cord.eoeo.momentwo.core.datastore.StoredSession
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
internal class AuthSnapshot(val session:StoredSession)
class AuthInterceptor @Inject constructor(private val preferenceRepository:PreferenceRepository):Interceptor{
    override fun intercept(chain:Interceptor.Chain):Response=runBlocking{
        val request=chain.request()
        if(request.header("isAuthorizable")=="false") return@runBlocking chain.proceed(request.newBuilder().removeHeader("isAuthorizable").removeHeader("Authorization").build())
        val session=preferenceRepository.getSession().getOrThrow() ?: throw IOException("계정 연결이 필요합니다")
        if(!session.identity.canSync)throw IOException("계정 인증이 만료되었습니다")
        val response=chain.proceed(request.newBuilder().tag(AuthSnapshot::class.java,AuthSnapshot(session)).header("Authorization","Bearer ${session.accessToken}").build())
        val current=preferenceRepository.getSession().getOrThrow()?.identity
        if(current?.accountId!=session.identity.accountId || current.epoch!=session.identity.epoch){response.close();throw IOException("계정 연결이 변경되었습니다")}
        response
    }
}
