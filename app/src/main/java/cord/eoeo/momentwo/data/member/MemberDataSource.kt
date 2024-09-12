package cord.eoeo.momentwo.data.member

import cord.eoeo.momentwo.data.model.EditMembers
import cord.eoeo.momentwo.data.model.InviteMembers
import cord.eoeo.momentwo.data.model.KickMembers
import cord.eoeo.momentwo.data.model.MemberList

interface MemberDataSource {
    suspend fun exitFromAlbum(albumId: Int): Result<Unit>

    suspend fun requestInviteMembers(
        albumId: Int,
        inviteMembers: InviteMembers,
    ): Result<Unit>

    suspend fun getMemberList(albumId: Int): Result<MemberList>

    suspend fun kickMembers(
        albumId: Int,
        kickMembers: KickMembers,
    ): Result<Unit>

    suspend fun assignAdminToMember(
        albumId: Int,
        nickname: String,
    ): Result<Unit>

    suspend fun editMembersPermission(
        albumId: Int,
        editMembers: EditMembers,
    ): Result<Unit>
}
