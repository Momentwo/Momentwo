package cord.eoeo.momentwo.core.data.signup

import cord.eoeo.momentwo.core.data.model.User
import cord.eoeo.momentwo.core.data.signup.remote.*
import cord.eoeo.momentwo.core.datastore.*
import cord.eoeo.momentwo.core.model.AccountIdentity
import cord.eoeo.momentwo.core.network.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import mockwebserver3.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException

class SignUpRequestTest {
    private class NoSessionReads : PreferenceRepository {
        override fun identities():Flow<AccountIdentity?> = throw AssertionError("Unexpected identities")
        override suspend fun getSession():Result<StoredSession?> = throw AssertionError("Signup must not read a session")
        override suspend fun storeSession(accountId:String,accessToken:String,refreshToken:String):Unit = throw AssertionError("Unexpected storeSession")
        override suspend fun updateSessionTokens(session:StoredSession,accessToken:String,refreshToken:String):Boolean = throw AssertionError("Unexpected updateSessionTokens")
        override suspend fun clearSession():Unit = throw AssertionError("Unexpected clearSession")
        override suspend fun expireSession(accountId:String,epoch:String):Unit = throw AssertionError("Unexpected expireSession")
        override suspend fun storeAccessToken(accessToken:String):Unit = throw AssertionError("Unexpected token storage")
        override suspend fun storeRefreshToken(refreshToken:String):Unit = throw AssertionError("Unexpected token storage")
        override suspend fun getAccessToken():Result<String> = throw AssertionError("Unexpected accessToken")
        override suspend fun getRefreshToken():Result<String> = throw AssertionError("Unexpected refreshToken")
    }
    @Test fun signupUsesExistingWirePayloadAndSkipsSessionAndAuthentication() = runTest {
        val server=MockWebServer();server.enqueue(MockResponse(code=204));server.start()
        val client=OkHttpClient.Builder().addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header("Authorization","Bearer fixture-must-be-removed").build())
        }.addInterceptor(AuthInterceptor(NoSessionReads())).build()
        try {
            val retrofit=ApiModule.provideRetrofit(client,ApiModule.provideJsonParser()).newBuilder().baseUrl(server.url("/")).build()
            val repository=SignUpRepositoryImpl(SignUpRemoteDataSource(retrofit.create(SignUpService::class.java),Dispatchers.IO))
            assertTrue(repository.requestSignUp(signUpFixture).isSuccess)
            val request=requireNotNull(server.takeRequest(5,TimeUnit.SECONDS))
            assertEquals("POST",request.method);assertEquals("/signup",request.target)
            assertTrue(request.headers["Content-Type"]!!.startsWith("application/json"))
            assertNull(request.headers["Authorization"]);assertNull(request.headers["isAuthorizable"])
            val json=ApiModule.provideJsonParser().adapter(Map::class.java).fromJson(requireNotNull(request.body).utf8())!!
            assertEquals(mapOf("name" to signUpFixture.name,"username" to signUpFixture.email,"password" to signUpFixture.password,"nickname" to signUpFixture.nickname,"birthday" to "2000-01-02","phone" to "010-1234-5678"),json)
            assertEquals(1,server.requestCount)
        } finally { client.dispatcher.executorService.shutdown();client.connectionPool.evictAll();server.close() }
    }
    @Test fun dtoSerializationKeepsSameSixNonNullFields() {
        val moshi=ApiModule.provideJsonParser();val adapter=moshi.adapter(User::class.java)
        val dto=signUpFixture.toUserDto()
        assertEquals(dto,adapter.fromJson(adapter.toJson(dto)))
        val json=moshi.adapter(Map::class.java).fromJson(adapter.toJson(dto))!!
        assertEquals(setOf("name","username","password","nickname","birthday","phone"),json.keys)
        assertTrue(json.values.none{it==null})
    }
    @Test fun httpFailureIsReturnedWithoutLosingStatusCode() = runTest {
        val server=MockWebServer();server.enqueue(MockResponse(code=409));server.start()
        val client=OkHttpClient.Builder().addInterceptor(AuthInterceptor(NoSessionReads())).build()
        try {
            val retrofit=ApiModule.provideRetrofit(client,ApiModule.provideJsonParser()).newBuilder().baseUrl(server.url("/")).build()
            val result=SignUpRepositoryImpl(SignUpRemoteDataSource(retrofit.create(SignUpService::class.java),Dispatchers.IO)).requestSignUp(signUpFixture)
            assertEquals(409,(result.exceptionOrNull() as HttpException).code());assertEquals(1,server.requestCount)
        } finally {client.dispatcher.executorService.shutdown();client.connectionPool.evictAll();server.close()}
    }
    @Test fun existingLoginAndProfileResponseStillDecodesAfterSignupDtoVisibilityChange() {
        val profile=ApiModule.provideJsonParser().adapter(cord.eoeo.momentwo.core.data.model.UserProfile::class.java)
            .fromJson("""{"name":"Fixture","username":"fixture@example.com","nickname":"fixture","phone":"01012345678","userProfileImage":"image"}""")!!
        assertEquals("Fixture",profile.name);assertEquals("fixture@example.com",profile.username)
        assertEquals("fixture",profile.nickname);assertEquals("01012345678",profile.phone);assertEquals("image",profile.userProfileImage)
    }

}
