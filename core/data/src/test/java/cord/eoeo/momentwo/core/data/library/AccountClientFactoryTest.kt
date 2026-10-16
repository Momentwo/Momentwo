package cord.eoeo.momentwo.core.data.library
import cord.eoeo.momentwo.core.datastore.*
import cord.eoeo.momentwo.core.model.AccountIdentity
import cord.eoeo.momentwo.core.network.ApiRequestCounter
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import mockwebserver3.*
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.POST
import org.junit.Assert.*
import org.junit.Test
class AccountClientFactoryTest {
    private interface Api { @GET("read") suspend fun read():ResponseBody;@POST("write") suspend fun write():ResponseBody }
    private class Preferences:PreferenceRepository{
        val current=AtomicReference<StoredSession?>(StoredSession(AccountIdentity("A","one",true),"a","ra"))
        override fun identities()=flowOf(current.get()?.identity)
        override suspend fun getSession()=Result.success(current.get())
        override suspend fun storeSession(accountId:String,accessToken:String,refreshToken:String){current.set(StoredSession(AccountIdentity(accountId,"new",true),accessToken,refreshToken))}
        override suspend fun clearSession(){current.set(null)}
        override suspend fun expireSession(accountId:String,epoch:String)=Unit
        override suspend fun updateSessionTokens(session:StoredSession,accessToken:String,refreshToken:String)=false
        override suspend fun storeAccessToken(accessToken:String)=Unit;override suspend fun storeRefreshToken(refreshToken:String)=Unit
        override suspend fun getAccessToken()=Result.success("");override suspend fun getRefreshToken()=Result.success("")
    }
    @Test fun accountSwitchAfterSendingCannotReturnDataOrUseNewOwnersToken()=runTest{
        val preferences=Preferences();val server=MockWebServer()
        server.dispatcher=object:Dispatcher(){override fun dispatch(request:RecordedRequest):MockResponse{preferences.current.set(StoredSession(AccountIdentity("B","two",true),"b","rb"));return MockResponse(body="old owner")}}
        server.start()
        try{
            val factory=AccountClientFactory(preferences,Retrofit.Builder().baseUrl(server.url("/")).client(OkHttpClient()).build(),ApiRequestCounter())
            val api=factory.client(factory.snapshot()).create(Api::class.java)
            try{withContext(Dispatchers.IO){api.read().close()};fail("Expected stale account rejection")}catch(_:IOException){}
            assertEquals("Bearer a",requireNotNull(server.takeRequest(5,TimeUnit.SECONDS)).headers["Authorization"]);assertEquals("B",preferences.current.get()!!.identity.accountId)
            assertEquals(1,server.requestCount)
        }finally{server.close()}
    }
    @Test fun ambiguousPostIsNotAutomaticallyReplayed()=runTest{
        val preferences=Preferences();val server=MockWebServer();server.enqueue(MockResponse.Builder().onResponseStart(SocketEffect.CloseSocket()).build());server.start()
        try{
            val factory=AccountClientFactory(preferences,Retrofit.Builder().baseUrl(server.url("/")).client(OkHttpClient()).build(),ApiRequestCounter())
            try{withContext(Dispatchers.IO){factory.client(factory.snapshot()).create(Api::class.java).write().close()};fail("Expected ambiguous disconnect")}catch(_:IOException){}
            assertEquals(1,server.requestCount)
        }finally{server.close()}
    }
    @Test fun oldSnapshotCannotSendAfterAccountChanges()=runTest{
        val preferences=Preferences();val server=MockWebServer();server.start()
        try{
            val factory=AccountClientFactory(preferences,Retrofit.Builder().baseUrl(server.url("/")).client(OkHttpClient()).build(),ApiRequestCounter());val snapshot=factory.snapshot()
            preferences.current.set(StoredSession(AccountIdentity("B","two",true),"b","rb"))
            try{withContext(Dispatchers.IO){factory.client(snapshot).create(Api::class.java).read().close()};fail("Expected account check")}catch(_:IOException){}
            assertEquals(0,server.requestCount)
        }finally{server.close()}
    }
}
