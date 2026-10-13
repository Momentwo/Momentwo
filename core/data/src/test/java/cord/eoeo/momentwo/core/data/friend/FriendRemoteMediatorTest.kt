package cord.eoeo.momentwo.core.data.friend

import androidx.paging.*
import cord.eoeo.momentwo.core.data.model.*
import cord.eoeo.momentwo.core.database.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
class FriendRemoteMediatorTest {
    private val local = FakeFriendLocal()
    private val remote = FakeFriendRemote()
    private fun mediator() = FriendRemoteMediator(remote, local).apply { setParams(10) }
    private fun state() = PagingState<Int, FriendEntity>(emptyList(), null, PagingConfig(10), 0)
    @Test fun terminalPageIsPersistedBeforeEndSignal() = runTest {
        remote.page = Result.success(FriendPage(listOf(FriendInfo(1, "last", "image")), null))
        val result = mediator().load(LoadType.REFRESH, state()) as RemoteMediator.MediatorResult.Success
        assertTrue(result.endOfPaginationReached)
        assertEquals(listOf(FriendEntity("last", "image")), local.items)
        assertNotNull(local.key)
        assertNull(local.key?.nextCursor)
    }
    @Test fun emptyRefreshClearsOldItemsAndRecordsTerminalKey() = runTest {
        local.items += FriendEntity("old", "")
        remote.page = Result.success(FriendPage(emptyList(), null))
        val result = mediator().load(LoadType.REFRESH, state()) as RemoteMediator.MediatorResult.Success
        assertTrue(result.endOfPaginationReached)
        assertTrue(local.items.isEmpty())
        assertNotNull(local.key)
    }
    @Test fun cancellationIsNotMediatorErrorAndDoesNotClearCache() = runTest {
        local.items += FriendEntity("old", "")
        remote.page = Result.failure(CancellationException("cancelled"))
        try { mediator().load(LoadType.REFRESH, state()); fail("Expected cancellation") }
        catch (_: CancellationException) { }
        assertEquals("old", local.items.single().nickname)
    }
}
private class FakeFriendLocal : FriendDataSource.Local {
    val items = mutableListOf<FriendEntity>()
    var key: FriendRemoteKeyEntity? = null
    override fun getPhotoPagingSource(): PagingSource<Int, FriendEntity> = error("unused")
    override suspend fun getFriendList() = Result.success(items.toList())
    override suspend fun insertFriends(friends: List<FriendEntity>) { items += friends }
    override suspend fun deleteByNicknames(friendNicknames: List<String>) { items.removeAll { it.nickname in friendNicknames } }
    override suspend fun deleteAll() { items.clear() }
    override suspend fun getLastKey() = key
    override suspend fun insertKey(key: FriendRemoteKeyEntity) { this.key = key }
    override suspend fun clearKeys() { key = null }
}
private class FakeFriendRemote : FriendDataSource.Remote {
    var page: Result<FriendPage> = Result.success(FriendPage(emptyList(), null))
    override suspend fun getFriendPage(size: Int, cursor: Int) = page
    override suspend fun sendFriendRequest(nickname: String) = Result.success(Unit)
    override suspend fun responseFriendRequest(nickname: String, isAccepted: Boolean) = Result.success(Unit)
    override suspend fun cancelFriendRequest(userId: Int) = Result.success(Unit)
    override suspend fun getSentRequests() = Result.success(SentFriendRequestList(emptyList()))
    override suspend fun getReceivedRequests() = Result.success(ReceivedFriendRequestList(emptyList()))
    override suspend fun getSearchUsers(nickname: String, page: Int, size: Int): Result<SearchUser> = error("unused")
}
