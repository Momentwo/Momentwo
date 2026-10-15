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
import cord.eoeo.momentwo.core.testing.EffectRecorder
import java.io.IOException
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val store = ViewModelStore()
    private val repository = FakeFriendRepository()
    private fun vm() = FriendViewModel(repository).also { store.put("friend", it) }
    @After fun clear() { store.clear(); main.testDispatcher.scheduler.runCurrent() }

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
    @Test fun requestTabsRetryInitialErrorAndRetainContentOnRefreshFailure() = runTest(main.testDispatcher) {
        val vm=vm(); val effects=EffectRecorder(vm.effect,backgroundScope)
        for(tab in RequestTab.entries) {
            val event=if(tab==RequestTab.Received)FriendEvent.OnGetReceivedRequests else FriendEvent.OnGetSentRequests
            repository.requests={Result.failure(IOException("offline"))}
            vm.onEvent(event); runCurrent(); assertEquals(ScreenState.Error(UiError.Network),vm.uiState.value.requests(tab))
            repository.requests={Result.success(listOf(FriendRequestItem(7,"seven","",false)))}
            vm.onEvent(event); runCurrent(); assertTrue(vm.uiState.value.requests(tab) is ScreenState.Success)
            repository.requests={Result.failure(IOException("refresh"))}
            vm.onEvent(event); runCurrent()
            val content=vm.uiState.value.requests(tab) as ScreenState.Success
            assertEquals(7,content.content.requests.single().userId)
            assertEquals(RefreshState.Error(UiError.Network),content.refresh)
        }
        assertTrue(effects.values.isEmpty())
    }
    @Test fun searchRejectsBlankAndStaleQueryAndDismissCancelsLateResponse() = runTest(main.testDispatcher) {
        val vm=vm(); val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(FriendEvent.OnGetSearchUser("")); runCurrent(); assertTrue(repository.searches.isEmpty())
        vm.onEvent(FriendEvent.OnSearchQueryChanged("new"))
        vm.onEvent(FriendEvent.OnGetSearchUser("old")); runCurrent(); assertTrue(repository.searches.isEmpty())
        val response=CompletableDeferred<Unit>()
        repository.search={ withContext(NonCancellable){response.await()}; flowOf(PagingData.empty()) }
        vm.onEvent(FriendEvent.OnClickRequestFriend); vm.onEvent(FriendEvent.OnGetSearchUser("new"));runCurrent()
        val source=vm.uiState.value.searchUserPagingData
        vm.onEvent(FriendEvent.OnDismissRequestFriend); response.complete(Unit);runCurrent()
        assertSame(source,vm.uiState.value.searchUserPagingData)
        assertFalse(vm.uiState.value.isRequestDialogOpened);assertEquals(listOf("new"),repository.searches)
        assertTrue(effects.values.isEmpty())
    }
    @Test fun failedOrCancelledWritesKeepDialogAndEmitOnlyFailureMessage() = runTest(main.testDispatcher) {
        val vm=vm();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(FriendEvent.OnClickRequestFriend)
        val events=listOf(FriendEvent.OnSendFriendRequest("seven"),FriendEvent.OnResponseFriendRequest(7,"seven",true),FriendEvent.OnResponseFriendRequest(7,"seven",false),FriendEvent.OnCancelFriendRequest(7))
        for(event in events){
            repository.operation={Result.failure(IOException("offline"))};vm.onEvent(event);runCurrent()
            assertTrue(vm.uiState.value.pendingWrites.isEmpty());assertTrue(vm.uiState.value.isRequestDialogOpened)
        }
        assertEquals(4,effects.values.size);assertTrue(effects.values.all{it is FriendEffect.ShowSnackbar})
        repository.operation={Result.failure(CancellationException())}
        events.forEach{vm.onEvent(it);runCurrent()};assertEquals(4,effects.values.size)
    }
    @Test fun acceptedRejectedAndCancelledRequestsChangeOnlyTheirId() = runTest(main.testDispatcher) {
        val vm=vm(); repository.requests={Result.success(listOf(FriendRequestItem(1,"one","",false),FriendRequestItem(2,"two","",false)))}
        vm.onEvent(FriendEvent.OnGetReceivedRequests);vm.onEvent(FriendEvent.OnGetSentRequests);runCurrent()
        vm.onEvent(FriendEvent.OnResponseFriendRequest(1,"one",false));runCurrent()
        assertEquals(listOf(1),(vm.uiState.value.received as ScreenState.Success).content.requests.filter{it.isUpdated}.map{it.userId})
        vm.onEvent(FriendEvent.OnCancelFriendRequest(1));runCurrent()
        assertEquals(listOf(1),(vm.uiState.value.sent as ScreenState.Success).content.requests.filter{it.isUpdated}.map{it.userId})
        vm.onEvent(FriendEvent.OnResponseFriendRequest(2,"two",true));runCurrent()
        assertEquals(1L,vm.uiState.value.friendListRevision)
        assertEquals(listOf("one" to false,"two" to true),repository.responseArgs)
        assertEquals(listOf(1),repository.cancelledIds)
    }
    @Test fun backEffectIsSingleAndClearPreventsNewEvents() = runTest(main.testDispatcher) {
        val vm=vm();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(FriendEvent.OnBack);runCurrent();assertEquals(listOf(FriendEffect.PopBackStack),effects.values)
        store.clear();vm.onEvent(FriendEvent.OnBack);runCurrent();assertEquals(1,effects.values.size)
    }

    @Test fun failedSearchKeepsQueryAndCancellationProducesNoSecondMessage()=runTest(main.testDispatcher){
        val vm=vm();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(FriendEvent.OnClickRequestFriend);vm.onEvent(FriendEvent.OnSearchQueryChanged("query"))
        repository.search={throw IOException("search")};vm.onEvent(FriendEvent.OnGetSearchUser("query"));runCurrent()
        assertEquals("query",vm.uiState.value.searchQuery);assertTrue(vm.uiState.value.isRequestDialogOpened)
        assertEquals(1,effects.values.size);assertTrue(effects.values.single() is FriendEffect.ShowSnackbar)
        repository.search={throw CancellationException()};vm.onEvent(FriendEvent.OnGetSearchUser("query"));runCurrent()
        assertEquals(1,effects.values.size);assertEquals(listOf("query","query"),repository.searches)
    }
    @Test fun latestSearchKeepsSuccessfulSourceAfterOlderQueryCompletes()=runTest(main.testDispatcher){
        val old=CompletableDeferred<Unit>();val vm=vm();val effects=EffectRecorder(vm.effect,backgroundScope)
        repository.search={if(repository.searches.last()=="old")withContext(NonCancellable){old.await()};flowOf(PagingData.empty())}
        vm.onEvent(FriendEvent.OnClickRequestFriend);vm.onEvent(FriendEvent.OnSearchQueryChanged("old"))
        vm.onEvent(FriendEvent.OnGetSearchUser("old"));runCurrent()
        vm.onEvent(FriendEvent.OnSearchQueryChanged("latest"));val empty=vm.uiState.value.searchUserPagingData
        vm.onEvent(FriendEvent.OnGetSearchUser("latest"));runCurrent()
        val latest=vm.uiState.value.searchUserPagingData;assertNotSame(empty,latest)
        old.complete(Unit);runCurrent();assertSame(latest,vm.uiState.value.searchUserPagingData)
        assertEquals("latest",vm.uiState.value.searchQuery);assertTrue(effects.values.isEmpty())
    }

}

private class FakeFriendRepository : FriendRepository {
    var pagingCalls = 0; var receivedCalls = 0; var sentCalls = 0; var responses = 0
    val responseArgs=mutableListOf<Pair<String,Boolean>>()
    val cancelledIds=mutableListOf<Int>()
    var requests: (suspend () -> Result<List<FriendRequestItem>>)? = null
    var operation: (suspend () -> Result<Unit>)? = null
    val searches = mutableListOf<String>()
    var search: suspend () -> Flow<PagingData<UserItem>> = { flowOf(PagingData.empty()) }
    var received: suspend () -> Result<List<FriendRequestItem>> = { Result.success(emptyList()) }
    var response: suspend () -> Result<Unit> = { Result.success(Unit) }
    override suspend fun getFriendPage(size: Int): Flow<PagingData<FriendItem>> { pagingCalls++; return flowOf(PagingData.empty()) }
    override suspend fun getFriendList() = Result.success(emptyList<FriendItem>())
    override suspend fun getReceivedRequests(): Result<List<FriendRequestItem>> { receivedCalls++; return requests?.invoke() ?: received() }
    override suspend fun getSentRequests(): Result<List<FriendRequestItem>> { sentCalls++; return requests?.invoke() ?: Result.success(emptyList()) }
    override suspend fun sendFriendRequest(nickname: String) = operation?.invoke() ?: Result.success(Unit)
    override suspend fun responseFriendRequest(nickname: String, isAccepted: Boolean): Result<Unit> { responses++; responseArgs+=nickname to isAccepted; return operation?.invoke() ?: response() }
    override suspend fun cancelFriendRequest(userId: Int):Result<Unit> { cancelledIds+=userId; return operation?.invoke() ?: Result.success(Unit) }
    override suspend fun getSearchUsers(nickname: String, size: Int): Flow<PagingData<UserItem>> { searches += nickname; return search() }
}
