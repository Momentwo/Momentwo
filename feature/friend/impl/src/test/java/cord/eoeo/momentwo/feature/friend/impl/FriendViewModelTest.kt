package cord.eoeo.momentwo.feature.friend.impl

import androidx.lifecycle.ViewModelStore
import androidx.paging.PagingData
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.friend.FriendRepository
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val store = ViewModelStore()
    private val repository = FakeFriendRepository()
    private fun vm() = FriendViewModel(repository).also { store.put("friend", it) }
    @After fun clear() { store.clear() }

    @Test fun onlySelectedRequestTabIsLoadedAndInitializationIsIdempotent() = runTest(main.testDispatcher) {
        val vm = vm()
        vm.initialize(); vm.initialize()
        vm.onEvent(FriendEvent.OnSelectRequestTab(RequestTab.Sent))
        advanceUntilIdle()
        assertEquals(1, repository.pagingCalls)
        assertEquals(0, repository.receivedCalls)
        assertEquals(1, repository.sentCalls)
        assertTrue(vm.uiState.value.sent is ScreenState.Success)
        assertEquals(ScreenState.Loading, vm.uiState.value.received)
    }
    @Test fun latestQueryWinsReverseCompletion() = runTest(main.testDispatcher) {
        val old = CompletableDeferred<List<FriendRequestItem>>()
        val newest = CompletableDeferred<List<FriendRequestItem>>()
        var count = 0
        repository.received = { Result.success(if (++count == 1) old.await() else newest.await()) }
        val vm = vm()
        vm.onEvent(FriendEvent.OnGetReceivedRequests); runCurrent()
        vm.onEvent(FriendEvent.OnGetReceivedRequests); runCurrent()
        newest.complete(listOf(FriendRequestItem(2, "latest", "", false))); runCurrent()
        old.complete(listOf(FriendRequestItem(1, "old", "", false))); advanceUntilIdle()
        assertEquals(2, (vm.uiState.value.received as ScreenState.Success).content.requests.single().userId)
    }
    @Test fun duplicateResponseIsIgnoredAndInputChangesAreKept() = runTest(main.testDispatcher) {
        val pending = CompletableDeferred<Unit>()
        repository.response = { pending.await(); Result.success(Unit) }
        val vm = vm()
        vm.onEvent(FriendEvent.OnResponseFriendRequest(1, "one", true)); runCurrent()
        vm.onEvent(FriendEvent.OnResponseFriendRequest(1, "one", false))
        vm.onEvent(FriendEvent.OnSelectRequestTab(RequestTab.Sent))
        vm.onEvent(FriendEvent.OnClickRequestFriend)
        pending.complete(Unit); advanceUntilIdle()
        assertEquals(1, repository.responses)
        assertEquals(RequestTab.Sent, vm.uiState.value.requestTab)
        assertTrue(vm.uiState.value.isRequestDialogOpened)
        assertTrue(vm.uiState.value.pendingWrites.isEmpty())
        assertEquals(1L, vm.uiState.value.friendListRevision)
    }
    @Test fun cancelledReadDoesNotBecomeError() = runTest(main.testDispatcher) {
        repository.received = { Result.failure(CancellationException("cancelled")) }
        val vm = vm()
        vm.onEvent(FriendEvent.OnGetReceivedRequests); advanceUntilIdle()
        assertEquals(ScreenState.Loading, vm.uiState.value.received)
    }
}

private class FakeFriendRepository : FriendRepository {
    var pagingCalls = 0; var receivedCalls = 0; var sentCalls = 0; var responses = 0
    var received: suspend () -> Result<List<FriendRequestItem>> = { Result.success(emptyList()) }
    var response: suspend () -> Result<Unit> = { Result.success(Unit) }
    override suspend fun getFriendPage(size: Int): Flow<PagingData<FriendItem>> { pagingCalls++; return flowOf(PagingData.empty()) }
    override suspend fun getFriendList() = Result.success(emptyList<FriendItem>())
    override suspend fun getReceivedRequests(): Result<List<FriendRequestItem>> { receivedCalls++; return received() }
    override suspend fun getSentRequests(): Result<List<FriendRequestItem>> { sentCalls++; return Result.success(emptyList()) }
    override suspend fun sendFriendRequest(nickname: String) = Result.success(Unit)
    override suspend fun responseFriendRequest(nickname: String, isAccepted: Boolean): Result<Unit> { responses++; return response() }
    override suspend fun cancelFriendRequest(userId: Int) = Result.success(Unit)
    override suspend fun getSearchUsers(nickname: String, size: Int): Flow<PagingData<UserItem>> = flowOf(PagingData.empty())
}
