package cord.eoeo.momentwo.data.login.remote

import cord.eoeo.momentwo.data.MomentwoApi
import cord.eoeo.momentwo.data.model.LoginRequest
import cord.eoeo.momentwo.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginService {
    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_LOGIN)
    suspend fun postLogin(
        @Body loginData: LoginRequest,
    ): LoginResponse
}
