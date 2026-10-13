package cord.eoeo.momentwo.feature.album.impl

import android.net.Uri
import androidx.paging.PagingData
import cord.eoeo.momentwo.core.data.album.AlbumRepository
import cord.eoeo.momentwo.core.data.friend.FriendRepository
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class AlbumFakeRepository : AlbumRepository {
    var read: suspend () -> Result<List<AlbumItem>> = { Result.success(emptyList()) }
    var create: suspend () -> Result<Unit> = { Result.success(Unit) }
    var reads = 0
    val creations = mutableListOf<Pair<String, List<String>>>()
    override suspend fun getAlbumList(): Result<List<AlbumItem>> { reads++; return read() }
    override suspend fun requestCreateAlbum(title: String, inviteList: List<String>): Result<Unit> { creations += title to inviteList.toList(); return create() }
    override suspend fun getAlbumRole(albumId: Int) = Result.success(MemberAuth.ADMIN)
    override suspend fun deleteAlbum(albumId: Int) = Result.success(Unit)
    override suspend fun changeAlbumImage(albumId: Int, profileImage: Uri) = Result.success(Unit)
    override suspend fun deleteAlbumImage(albumId: Int) = Result.success(Unit)
    override suspend fun changeAlbumSubTitle(albumId: Int, subTitle: String) = Result.success(Unit)
    override suspend fun deleteAlbumSubTitle(albumId: Int) = Result.success(Unit)
    override suspend fun changeAlbumTitle(albumId: Int, title: String) = Result.success(Unit)
}
internal class AlbumFriendRepository : FriendRepository {
    var read: suspend () -> Result<List<FriendItem>> = { Result.success(listOf(FriendItem("first", ""), FriendItem("second", ""))) }
    override suspend fun getFriendList() = read()
    override suspend fun sendFriendRequest(nickname: String) = Result.success(Unit)
    override suspend fun responseFriendRequest(nickname: String, isAccepted: Boolean) = Result.success(Unit)
    override suspend fun cancelFriendRequest(userId: Int) = Result.success(Unit)
    override suspend fun getFriendPage(size: Int): Flow<PagingData<FriendItem>> = flowOf(PagingData.empty())
    override suspend fun getSentRequests() = Result.success(emptyList<FriendRequestItem>())
    override suspend fun getReceivedRequests() = Result.success(emptyList<FriendRequestItem>())
    override suspend fun getSearchUsers(nickname: String, size: Int): Flow<PagingData<UserItem>> = flowOf(PagingData.empty())
}
