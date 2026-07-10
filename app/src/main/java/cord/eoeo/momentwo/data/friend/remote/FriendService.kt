package cord.eoeo.momentwo.data.friend.remote

import cord.eoeo.momentwo.core.network.MomentwoApi
import cord.eoeo.momentwo.data.model.FriendPage
import cord.eoeo.momentwo.data.model.FriendRequestResponse
import cord.eoeo.momentwo.data.model.ReceivedFriendRequestList
import cord.eoeo.momentwo.data.model.SearchUser
import cord.eoeo.momentwo.data.model.SentFriendRequestList
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendService {
    @FormUrlEncoded
    @POST(MomentwoApi.POST_FRIEND_REQUEST)
    suspend fun postFriendRequest(
        @Field("nickname") nickname: String,
    )

    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_RESPONSE_FRIEND_REQUEST)
    suspend fun postResponseFriendRequest(
        @Body friendRequestResponse: FriendRequestResponse,
    )

    @DELETE(MomentwoApi.DELETE_FRIEND_REQUEST)
    suspend fun deleteFriendRequest(
        @Path("userId") userId: Int,
    )

    @GET(MomentwoApi.GET_FRIEND_PAGE)
    suspend fun getFriendList(
        @Query("size") size: Int,
        @Query("cursor") cursor: Int,
    ): FriendPage

    @GET(MomentwoApi.GET_SENT_FRIEND_REQUESTS)
    suspend fun getSentRequests(): SentFriendRequestList

    @GET(MomentwoApi.GET_RECEIVED_FRIEND_REQUESTS)
    suspend fun getReceivedRequests(): ReceivedFriendRequestList

    @GET(MomentwoApi.GET_SEARCH_USERS)
    suspend fun getSearchUsers(
        @Query("nickname") nickname: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): SearchUser
}
