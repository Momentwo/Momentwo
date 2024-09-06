package cord.eoeo.momentwo.data.friend.remote

import cord.eoeo.momentwo.data.MomentwoApi
import cord.eoeo.momentwo.data.model.FriendList
import cord.eoeo.momentwo.data.model.FriendRequestResponse
import cord.eoeo.momentwo.data.model.ReceivedFriendRequestList
import cord.eoeo.momentwo.data.model.SentFriendRequestList
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

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

    @FormUrlEncoded
    @DELETE(MomentwoApi.DELETE_FRIEND_REQUEST)
    suspend fun deleteFriendRequest(
        @Field("nickname") nickname: String,
    )

    @GET(MomentwoApi.GET_FRIEND_LIST)
    suspend fun getFriendList(): FriendList

    @GET(MomentwoApi.GET_SENT_FRIEND_REQUESTS)
    suspend fun getSentRequests(): SentFriendRequestList

    @GET(MomentwoApi.GET_RECEIVED_FRIEND_REQUESTS)
    suspend fun getReceivedRequests(): ReceivedFriendRequestList
}
