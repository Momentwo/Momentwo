package cord.eoeo.momentwo.core.data.like.remote

import cord.eoeo.momentwo.core.network.MomentwoApi
import cord.eoeo.momentwo.core.data.model.LikeCount
import cord.eoeo.momentwo.core.data.model.LikeRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface LikeService {
    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_DO_LIKE)
    suspend fun postDoLike(
        @Body likeRequest: LikeRequest,
    )

    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_UNDO_LIKE)
    suspend fun postUndoLike(
        @Body likeRequest: LikeRequest,
    )

    @GET(MomentwoApi.GET_LIKE_COUNT)
    suspend fun getLikeCount(
        @Path("albumId") albumId: Int,
        @Path("photoId") photoId: Int,
    ): LikeCount
}
