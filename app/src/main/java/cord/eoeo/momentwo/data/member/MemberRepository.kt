package cord.eoeo.momentwo.data.member

import cord.eoeo.momentwo.ui.model.MemberItem

interface MemberRepository {
    suspend fun exitFromAlbum(albumId: Int): Result<Unit>

    suspend fun requestInviteMembers(
        albumId: Int,
        inviteMembers: List<String>,
    ): Result<Unit>

    suspend fun getMemberList(albumId: Int): Result<List<MemberItem>>

    suspend fun kickMembers(
        albumId: Int,
        kickMembers: List<String>,
    ): Result<Unit>

    suspend fun assignAdminToMember(
        albumId: Int,
        nickname: String,
    ): Result<Unit>

    suspend fun editMembersPermission(
        albumId: Int,
        editMembers: List<MemberItem>,
    ): Result<Unit>
}
