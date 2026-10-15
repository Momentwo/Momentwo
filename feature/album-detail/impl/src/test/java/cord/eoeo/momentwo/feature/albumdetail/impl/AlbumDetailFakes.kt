package cord.eoeo.momentwo.feature.albumdetail.impl

import android.net.Uri
import androidx.paging.PagingData
import cord.eoeo.momentwo.core.data.album.AlbumRepository
import cord.eoeo.momentwo.core.data.member.MemberRepository
import cord.eoeo.momentwo.core.data.subalbum.SubAlbumRepository
import cord.eoeo.momentwo.core.data.friend.FriendRepository
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class DetailAlbumRepository : AlbumRepository {
    var role: suspend () -> Result<MemberAuth> = { Result.success(MemberAuth.ADMIN) }
    var operation: suspend () -> Result<Unit> = { Result.success(Unit) }
    var title: suspend () -> Result<Unit> = { Result.success(Unit) }
    override suspend fun getAlbumRole(albumId: Int) = role()
    override suspend fun requestCreateAlbum(title: String, inviteList: List<String>) = Result.success(Unit)
    override suspend fun deleteAlbum(albumId: Int) = operation()
    override suspend fun changeAlbumImage(albumId: Int, profileImage: Uri) = operation()
    override suspend fun deleteAlbumImage(albumId: Int) = operation()
    override suspend fun changeAlbumSubTitle(albumId: Int, subTitle: String) = operation()
    override suspend fun deleteAlbumSubTitle(albumId: Int) = operation()
    override suspend fun changeAlbumTitle(albumId: Int, title: String) = this.title()
    override suspend fun getAlbumList() = Result.success(emptyList<AlbumItem>())
}
internal class DetailSubAlbumRepository : SubAlbumRepository {
    var items = listOf(SubAlbumItem(1, "one", emptyList()), SubAlbumItem(2, "two", emptyList()))
    var read: suspend () -> Result<List<SubAlbumItem>> = { Result.success(items) }
    var delete: suspend () -> Result<Unit> = { Result.success(Unit) }
    val deletions = mutableListOf<List<Int>>()
    var operation: suspend () -> Result<Unit> = { Result.success(Unit) }
    var creates = 0
    var reads = 0
    override suspend fun requestCreateSubAlbum(albumId: Int, title: String): Result<Unit> { creates++; return operation() }
    override suspend fun getSubAlbumList(albumId: Int): Result<List<SubAlbumItem>> { reads++; return read() }
    override suspend fun changeSubAlbumTitle(albumId: Int, subAlbumId: Int, title: String) = operation()
    override suspend fun deleteSubAlbums(albumId: Int, subAlbumIds: List<Int>): Result<Unit> {
        deletions += subAlbumIds.toList()
        return delete().onSuccess { items = items.filterNot { it.id in subAlbumIds } }
    }
}
internal class DetailMemberRepository : MemberRepository {
    var operation: suspend () -> Result<Unit> = { Result.success(Unit) }
    var read: suspend () -> Result<List<MemberItem>> = { Result.success(emptyList()) }
    var invite: suspend () -> Result<Unit> = { Result.success(Unit) }
    val invitations = mutableListOf<List<String>>()
    override suspend fun exitFromAlbum(albumId: Int) = operation()
    override suspend fun requestInviteMembers(albumId: Int, inviteMembers: List<String>): Result<Unit> { invitations += inviteMembers.toList(); return invite() }
    override suspend fun getMemberList(albumId: Int) = read()
    override suspend fun kickMembers(albumId: Int, kickMembers: List<Int>) = operation()
    override suspend fun assignAdminToMember(albumId: Int, nickname: String) = operation()
    override suspend fun editMembersPermission(albumId: Int, editMembers: List<MemberItem>) = operation()
}
internal class DetailFriendRepository : FriendRepository {
    override suspend fun getFriendList() = Result.success(listOf(FriendItem("first", ""), FriendItem("second", "")))
    override suspend fun sendFriendRequest(nickname: String) = Result.success(Unit)
    override suspend fun responseFriendRequest(nickname: String, isAccepted: Boolean) = Result.success(Unit)
    override suspend fun cancelFriendRequest(userId: Int) = Result.success(Unit)
    override suspend fun getFriendPage(size: Int): Flow<PagingData<FriendItem>> = flowOf(PagingData.empty())
    override suspend fun getSentRequests() = Result.success(emptyList<FriendRequestItem>())
    override suspend fun getReceivedRequests() = Result.success(emptyList<FriendRequestItem>())
    override suspend fun getSearchUsers(nickname: String, size: Int): Flow<PagingData<UserItem>> = flowOf(PagingData.empty())
}
