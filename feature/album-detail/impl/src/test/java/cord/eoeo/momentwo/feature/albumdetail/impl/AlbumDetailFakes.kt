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
    var title: suspend () -> Result<Unit> = { Result.success(Unit) }
    override suspend fun getAlbumRole(albumId: Int) = role()
    override suspend fun requestCreateAlbum(title: String, inviteList: List<String>) = Result.success(Unit)
    override suspend fun deleteAlbum(albumId: Int) = Result.success(Unit)
    override suspend fun changeAlbumImage(albumId: Int, profileImage: Uri) = Result.success(Unit)
    override suspend fun deleteAlbumImage(albumId: Int) = Result.success(Unit)
    override suspend fun changeAlbumSubTitle(albumId: Int, subTitle: String) = Result.success(Unit)
    override suspend fun deleteAlbumSubTitle(albumId: Int) = Result.success(Unit)
    override suspend fun changeAlbumTitle(albumId: Int, title: String) = this.title()
    override suspend fun getAlbumList() = Result.success(emptyList<AlbumItem>())
}
internal class DetailSubAlbumRepository : SubAlbumRepository {
    var items = listOf(SubAlbumItem(1, "one", emptyList()), SubAlbumItem(2, "two", emptyList()))
    var read: suspend () -> Result<List<SubAlbumItem>> = { Result.success(items) }
    var delete: suspend () -> Result<Unit> = { Result.success(Unit) }
    val deletions = mutableListOf<List<Int>>()
    var creates = 0
    var reads = 0
    override suspend fun requestCreateSubAlbum(albumId: Int, title: String): Result<Unit> { creates++; return Result.success(Unit) }
    override suspend fun getSubAlbumList(albumId: Int): Result<List<SubAlbumItem>> { reads++; return read() }
    override suspend fun changeSubAlbumTitle(albumId: Int, subAlbumId: Int, title: String) = Result.success(Unit)
    override suspend fun deleteSubAlbums(albumId: Int, subAlbumIds: List<Int>): Result<Unit> {
        deletions += subAlbumIds.toList()
        return delete().onSuccess { items = items.filterNot { it.id in subAlbumIds } }
    }
}
internal class DetailMemberRepository : MemberRepository {
    var invite: suspend () -> Result<Unit> = { Result.success(Unit) }
    val invitations = mutableListOf<List<String>>()
    override suspend fun exitFromAlbum(albumId: Int) = Result.success(Unit)
    override suspend fun requestInviteMembers(albumId: Int, inviteMembers: List<String>): Result<Unit> { invitations += inviteMembers.toList(); return invite() }
    override suspend fun getMemberList(albumId: Int) = Result.success(emptyList<MemberItem>())
    override suspend fun kickMembers(albumId: Int, kickMembers: List<Int>) = Result.success(Unit)
    override suspend fun assignAdminToMember(albumId: Int, nickname: String) = Result.success(Unit)
    override suspend fun editMembersPermission(albumId: Int, editMembers: List<MemberItem>) = Result.success(Unit)
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
