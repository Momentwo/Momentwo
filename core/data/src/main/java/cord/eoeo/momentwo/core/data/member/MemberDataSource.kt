package cord.eoeo.momentwo.core.data.member

import cord.eoeo.momentwo.core.data.model.AssignAdminToMember
import cord.eoeo.momentwo.core.data.model.EditMembers
import cord.eoeo.momentwo.core.data.model.InviteMembers
import cord.eoeo.momentwo.core.data.model.MemberList

interface MemberDataSource {
    suspend fun exitFromAlbum(albumId: Int): Result<Unit>

    suspend fun requestInviteMembers(inviteMembers: InviteMembers): Result<Unit>

    suspend fun getMemberList(albumId: Int): Result<MemberList>

    suspend fun kickMembers(
        albumId: Int,
        kickMemberIds: String,
    ): Result<Unit>

    suspend fun assignAdminToMember(assignAdminToMember: AssignAdminToMember): Result<Unit>

    suspend fun editMembersPermission(editMembers: EditMembers): Result<Unit>
}
