package cord.eoeo.momentwo.data.login.remote

import cord.eoeo.momentwo.core.network.MomentwoApi
import cord.eoeo.momentwo.data.model.LoginRequest
import cord.eoeo.momentwo.data.model.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginService {
    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_LOGIN)
    suspend fun postLogin(
        @Body loginData: LoginRequest,
    ): Response<UserProfile>
}
