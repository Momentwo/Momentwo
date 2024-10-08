package cord.eoeo.momentwo.data.member.remote

import cord.eoeo.momentwo.data.MomentwoApi
import cord.eoeo.momentwo.data.model.AssignAdminToMember
import cord.eoeo.momentwo.data.model.EditMembers
import cord.eoeo.momentwo.data.model.InviteMembers
import cord.eoeo.momentwo.data.model.KickMembers
import cord.eoeo.momentwo.data.model.MemberList
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MemberService {
    @FormUrlEncoded
    @DELETE(MomentwoApi.DELETE_EXIT_MEMBER)
    suspend fun deleteExitMember(
        @Body albumId: Int,
    )

    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_INVITE_MEMBERS)
    suspend fun postInviteMembers(
        @Body inviteMembers: InviteMembers,
    )

    @GET(MomentwoApi.GET_MEMBER_LIST)
    suspend fun getMemberList(
        @Path("albumId") albumId: Int,
    ): MemberList

    @Headers("content-type: application/json")
    @DELETE(MomentwoApi.DELETE_KICK_MEMBERS)
    suspend fun deleteKickMembers(
        @Body kickMembers: KickMembers,
    )

    @Headers("content-type: application/json")
    @PUT(MomentwoApi.PUT_MEMBER_ASSIGN_ADMIN)
    suspend fun putMemberAssignAdmin(
        @Body assignAdminToMember: AssignAdminToMember,
    )

    @Headers("content-type: application/json")
    @PUT(MomentwoApi.PUT_EDIT_MEMBERS_PERMISSION)
    suspend fun putEditMembersPermission(
        @Body editMembers: EditMembers,
    )
}
