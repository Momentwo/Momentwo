package cord.eoeo.momentwo.core.data.member.remote

import cord.eoeo.momentwo.core.network.MomentwoApi
import cord.eoeo.momentwo.core.data.model.AssignAdminToMember
import cord.eoeo.momentwo.core.data.model.EditMembers
import cord.eoeo.momentwo.core.data.model.InviteMembers
import cord.eoeo.momentwo.core.data.model.MemberList
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MemberService {
    @DELETE(MomentwoApi.DELETE_EXIT_MEMBER)
    suspend fun deleteExitMember(
        @Path("albumId") albumId: Int,
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

    @DELETE(MomentwoApi.DELETE_KICK_MEMBERS)
    suspend fun deleteKickMembers(
        @Path("albumId") albumId: Int,
        @Path("kickUsersId") kickUsersId: String,
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
