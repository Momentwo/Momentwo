package cord.eoeo.momentwo.core.data.member

import cord.eoeo.momentwo.core.data.model.AssignAdminToMember
import cord.eoeo.momentwo.core.data.model.EditMembers
import cord.eoeo.momentwo.core.data.model.InviteMembers
import cord.eoeo.momentwo.core.model.MemberItem

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
        return memberRemoteDataSource.requestInviteMembers(InviteMembers(albumId, inviteMembers))
    }

    override suspend fun getMemberList(albumId: Int): Result<List<MemberItem>> {
        // return Result.success(emptyList())
        return memberRemoteDataSource.getMemberList(albumId).map { memberList ->
            memberList.memberList.map { it.mapToMemberItem() }
        }
    }

    override suspend fun kickMembers(
        albumId: Int,
        kickMembers: List<Int>,
    ): Result<Unit> {
        // return Result.success(Unit)
        return memberRemoteDataSource.kickMembers(albumId, kickMembers.joinToString(","))
    }

    override suspend fun assignAdminToMember(
        albumId: Int,
        nickname: String,
    ): Result<Unit> {
        // return Result.success(Unit)
        return memberRemoteDataSource.assignAdminToMember(AssignAdminToMember(albumId, nickname))
    }

    override suspend fun editMembersPermission(
        albumId: Int,
        editMembers: List<MemberItem>,
    ): Result<Unit> {
        // return Result.success(Unit)
        return memberRemoteDataSource.editMembersPermission(
            EditMembers(albumId, editMembers.associate { it.nickname to it.auth.roleString }),
        )
    }
}
