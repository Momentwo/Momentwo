package cord.eoeo.momentwo.data.member.remote

import cord.eoeo.momentwo.data.member.MemberDataSource
import cord.eoeo.momentwo.data.model.AssignAdminToMember
import cord.eoeo.momentwo.data.model.EditMembers
import cord.eoeo.momentwo.data.model.InviteMembers
import cord.eoeo.momentwo.data.model.KickMembers
import cord.eoeo.momentwo.data.model.MemberList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemberRemoteDataSource(
    private val memberService: MemberService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MemberDataSource {
    override suspend fun exitFromAlbum(albumId: Int): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                memberService.deleteExitMember(albumId)
            }
        }

    override suspend fun requestInviteMembers(inviteMembers: InviteMembers): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                memberService.postInviteMembers(inviteMembers)
            }
        }

    override suspend fun getMemberList(albumId: Int): Result<MemberList> =
        runCatching {
            withContext(dispatcher) {
                memberService.getMemberList(albumId)
            }
        }

    override suspend fun kickMembers(kickMembers: KickMembers): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                memberService.deleteKickMembers(kickMembers)
            }
        }

    override suspend fun assignAdminToMember(assignAdminToMember: AssignAdminToMember): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                memberService.putMemberAssignAdmin(assignAdminToMember)
            }
        }

    override suspend fun editMembersPermission(editMembers: EditMembers): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                memberService.putEditMembersPermission(editMembers)
            }
        }
}
