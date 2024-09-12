package cord.eoeo.momentwo.data.member.remote

import cord.eoeo.momentwo.data.member.MemberDataSource
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

    override suspend fun requestInviteMembers(
        albumId: Int,
        inviteMembers: InviteMembers,
    ): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                memberService.postInviteMembers(albumId, inviteMembers)
            }
        }

    override suspend fun getMemberList(albumId: Int): Result<MemberList> =
        runCatching {
            withContext(dispatcher) {
                memberService.getMemberList(albumId)
            }
        }

    override suspend fun kickMembers(
        albumId: Int,
        kickMembers: KickMembers,
    ): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                memberService.deleteKickMembers(albumId, kickMembers)
            }
        }

    override suspend fun assignAdminToMember(
        albumId: Int,
        nickname: String,
    ): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                memberService.putMemberAssignAdmin(albumId, nickname)
            }
        }

    override suspend fun editMembersPermission(
        albumId: Int,
        editMembers: EditMembers,
    ): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                memberService.putEditMembersPermission(albumId, editMembers)
            }
        }
}
