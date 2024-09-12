package cord.eoeo.momentwo.data.member.remote

import cord.eoeo.momentwo.data.MomentwoApi
import cord.eoeo.momentwo.data.model.EditMembers
import cord.eoeo.momentwo.data.model.InviteMembers
import cord.eoeo.momentwo.data.model.KickMembers
import cord.eoeo.momentwo.data.model.MemberList
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MemberService {
    @Headers("content-type: application/json")
    @DELETE(MomentwoApi.DELETE_EXIT_MEMBER)
    suspend fun deleteExitMember(
        @Path("albumId") albumId: Int,
    )

    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_INVITE_MEMBERS)
    suspend fun postInviteMembers(
        @Path("albumId") albumId: Int,
        @Body inviteMembers: InviteMembers,
    )

    @Headers("content-type: application/json")
    @GET(MomentwoApi.GET_MEMBER_LIST)
    suspend fun getMemberList(
        @Path("albumId") albumId: Int,
    ): MemberList

    @Headers("content-type: application/json")
    @DELETE(MomentwoApi.DELETE_KICK_MEMBERS)
    suspend fun deleteKickMembers(
        @Path("albumId") albumId: Int,
        @Body kickMembers: KickMembers,
    )

    @FormUrlEncoded
    @PUT(MomentwoApi.PUT_MEMBER_ASSIGN_ADMIN)
    suspend fun putMemberAssignAdmin(
        @Path("albumId") albumId: Int,
        @Field("nickname") nickname: String,
    )

    @Headers("content-type: application/json")
    @PUT(MomentwoApi.PUT_EDIT_MEMBERS_PERMISSION)
    suspend fun putEditMembersPermission(
        @Path("albumId") albumId: Int,
        @Body editMembers: EditMembers,
    )
}
