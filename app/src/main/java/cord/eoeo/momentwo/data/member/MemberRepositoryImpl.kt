package cord.eoeo.momentwo.data.member

import cord.eoeo.momentwo.data.model.EditMembers
import cord.eoeo.momentwo.data.model.InviteMembers
import cord.eoeo.momentwo.data.model.KickMembers
import cord.eoeo.momentwo.data.model.MemberRule
import cord.eoeo.momentwo.ui.model.MemberItem

class MemberRepositoryImpl(
    private val memberRemoteDataSource: MemberDataSource,
) : MemberRepository {
    override suspend fun exitFromAlbum(albumId: Int): Result<Unit> {
        // return Result.success(Unit)
        return memberRemoteDataSource.exitFromAlbum(albumId)
    }

    override suspend fun requestInviteMembers(
        albumId: Int,
        inviteMembers: List<String>,
    ): Result<Unit> {
        // return Result.success(Unit)
        return memberRemoteDataSource.requestInviteMembers(albumId, InviteMembers(inviteMembers))
    }

    override suspend fun getMemberList(albumId: Int): Result<List<MemberItem>> {
        // return Result.success(emptyList())
        return memberRemoteDataSource.getMemberList(albumId).map { memberList ->
            memberList.memberList.map { it.mapToMemberItem() }
        }
    }

    override suspend fun kickMembers(
        albumId: Int,
        kickMembers: List<String>,
    ): Result<Unit> {
        // return Result.success(Unit)
        return memberRemoteDataSource.kickMembers(albumId, KickMembers(kickMembers))
    }

    override suspend fun assignAdminToMember(
        albumId: Int,
        nickname: String,
    ): Result<Unit> {
        // return Result.success(Unit)
        return memberRemoteDataSource.assignAdminToMember(albumId, nickname)
    }

    override suspend fun editMembersPermission(
        albumId: Int,
        editMembers: List<MemberItem>,
    ): Result<Unit> {
        // return Result.success(Unit)
        return memberRemoteDataSource.editMembersPermission(
            albumId,
            EditMembers(editMembers.associate { it.nickname to MemberRule.memberAuthToRule(it.auth) }),
        )
    }
}
