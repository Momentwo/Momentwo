package cord.eoeo.momentwo.data.profile.remote

import cord.eoeo.momentwo.core.network.MomentwoApi
import cord.eoeo.momentwo.data.model.UserProfile
import retrofit2.http.GET
import retrofit2.http.Query

interface ProfileService {
    @GET(MomentwoApi.GET_PROFILE)
    suspend fun getProfile(
        @Query("nickname") nickname: String,
    ): UserProfile
}
