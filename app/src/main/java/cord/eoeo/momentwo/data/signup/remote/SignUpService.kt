package cord.eoeo.momentwo.data.signup.remote

import cord.eoeo.momentwo.data.MomentwoApi
import cord.eoeo.momentwo.data.model.Email
import cord.eoeo.momentwo.data.model.Nickname
import cord.eoeo.momentwo.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SignUpService {
    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_SIGN_UP)
    suspend fun postSignUp(
        @Body user: User,
    )

    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_CHECK_EMAIL)
    suspend fun postCheckEmail(
        @Body email: Email,
    ): Response<Unit>

    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_CHECK_NICKNAME)
    suspend fun postCheckNickname(
        @Body nickname: Nickname,
    ): Response<Unit>
}
