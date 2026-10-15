package cord.eoeo.momentwo.feature.album.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.testing.*
import cord.eoeo.momentwo.feature.album.impl.account.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

internal class AccountAccessFake : StrictAccountLibraryAccess() {
    val identity = MutableStateFlow<AccountIdentity?>(null)
    var refresh: suspend () -> Unit = {}
    var logout: suspend () -> Unit = { identity.value = null }
    var refreshes = 0
    var logouts = 0
    override fun identities() = identity
    override suspend fun storage() = 10L to 100L
    override suspend fun refresh() { refreshes++; refresh.invoke() }
    override suspend fun logout() { logouts++; logout.invoke() }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AccountAccessViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val store = ViewModelStore()
    private val accounts = AccountAccessFake()
    private val local = LocalLibraryFake()
    private val a = AccountIdentity("A", "a1", true)
    private val b = AccountIdentity("B", "b1", true)
    private fun vm() = AccountLibraryViewModel(accounts, local).also { store.put("library", it) }
    private fun content(vm: AccountLibraryViewModel) = (vm.uiState.value.screen as ScreenState.Success).content
    @After fun clear() { store.clear(); main.testDispatcher.scheduler.runCurrent() }

    @Test fun gateSettlesWithoutAccountAndCannotReuseEarlierEpoch() = runTest(main.testDispatcher) {
        val vm = AccountGateViewModel(accounts).also { store.put("gate", it) }
        runCurrent(); assertTrue(vm.uiState.value.settled); assertFalse(vm.uiState.value.allowed)
        accounts.identity.value = a; runCurrent(); assertTrue(vm.uiState.value.allowed)
        accounts.identity.value = a.copy(canSync=false); runCurrent(); assertTrue(vm.uiState.value.allowed)
        accounts.identity.value = b; runCurrent(); assertFalse(vm.uiState.value.allowed)
        accounts.identity.value = a.copy(epoch="a2"); runCurrent(); assertFalse(vm.uiState.value.allowed)
        store.clear(); val before=vm.uiState.value
        accounts.identity.value=null; runCurrent(); assertEquals(before, vm.uiState.value)
    }
    @Test fun deviceObservationIsIdempotentAndNeverRefreshesAccountOnInitialize() = runTest(main.testDispatcher) {
        val vm=vm(); vm.initialize(); runCurrent(); vm.initialize(); runCurrent()
        assertNull(content(vm).identity); assertTrue(content(vm).albums.isEmpty())
        assertEquals(1,local.subscriptions["device"]); assertEquals(0,accounts.refreshes)
    }
    @Test fun switchingAccountsCancelsOldSubscriptionAndNeverShowsOldRows() = runTest(main.testDispatcher) {
        accounts.identity.value=a
        val vm=vm(); vm.initialize(); runCurrent()
        val row=LocalAlbum("a", "account:A", "private", "", "", null, 0)
        local.accountRows.getValue("account:A").value=listOf(row); runCurrent()
        assertEquals(listOf(row),content(vm).albums)
        accounts.identity.value=b; runCurrent()
        assertEquals(0,local.subscriptions["account:A"]); assertEquals(b,content(vm).identity)
        assertTrue(content(vm).albums.isEmpty())
        local.accountRows.getValue("account:A").value=listOf(row.copy(title="late")); runCurrent()
        assertTrue(content(vm).albums.isEmpty())
        accounts.identity.value=null; runCurrent(); assertNull(content(vm).identity)
        assertEquals(0,local.subscriptions["account:B"])
    }
    @Test fun failedRefreshKeepsCachedBodyAndEmptySuccessCanRefreshAgain() = runTest(main.testDispatcher) {
        accounts.identity.value=a; val vm=vm(); vm.initialize(); runCurrent()
        val call=ControlledCall<Unit>(); accounts.refresh={call.await()}
        vm.openAlbums(); runCurrent()
        assertEquals(RefreshState.Loading,(vm.uiState.value.screen as ScreenState.Success).refresh)
        call.fail(IOException("offline")); runCurrent()
        assertEquals(RefreshState.Error(UiError.Network),(vm.uiState.value.screen as ScreenState.Success).refresh)
        accounts.refresh={}; vm.openAlbums(); runCurrent()
        assertEquals(RefreshState.Idle,(vm.uiState.value.screen as ScreenState.Success).refresh)
        assertTrue(content(vm).albums.isEmpty()); assertEquals(2,accounts.refreshes)
    }
    @Test fun lateRefreshAfterAccountSwitchCannotFailNewAccount() = runTest(main.testDispatcher) {
        accounts.identity.value=a; val vm=vm(); vm.initialize(); runCurrent()
        val call=ControlledCall<Unit>()
        accounts.refresh={withContext(NonCancellable){call.await()}}
        vm.openAlbums(); runCurrent(); accounts.identity.value=b; runCurrent()
        call.fail(IOException("old")); runCurrent()
        assertEquals(b,content(vm).identity)
        assertEquals(RefreshState.Idle,(vm.uiState.value.screen as ScreenState.Success).refresh)
    }
    @Test fun logoutIgnoresDuplicateAndFailurePublishesExactlyOneMessage() = runTest(main.testDispatcher) {
        accounts.identity.value=a; val vm=vm(); vm.initialize(); runCurrent()
        val effects=EffectRecorder(vm.effect,backgroundScope); val call=ControlledCall<Unit>()
        accounts.logout={call.await()}
        vm.onEvent(AccountLibraryEvent.Logout); runCurrent(); vm.onEvent(AccountLibraryEvent.Logout)
        assertEquals(setOf("logout"),vm.uiState.value.pending)
        call.fail(IOException("storage")); runCurrent()
        assertEquals(1,accounts.logouts); assertEquals(1,effects.values.size)
        assertTrue(effects.values.single() is AccountLibraryEffect.Message)
        assertEquals(a,content(vm).identity); assertTrue(vm.uiState.value.pending.isEmpty())
        runCurrent(); assertEquals(1,effects.values.size)
    }
    @Test fun navigationEffectsAreOrderedAndClearingStopsObservationAndEffects() = runTest(main.testDispatcher) {
        val vm=vm(); val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.initialize(); runCurrent(); vm.onEvent(AccountLibraryEvent.Friend)
        vm.onEvent(AccountLibraryEvent.Sync); vm.onEvent(AccountLibraryEvent.Back); runCurrent()
        assertEquals(listOf(AccountLibraryEffect.Login,AccountLibraryEffect.Sync,AccountLibraryEffect.Back),effects.values)
        store.clear(); runCurrent(); val before=vm.uiState.value
        accounts.identity.value=a; vm.onEvent(AccountLibraryEvent.Login); runCurrent()
        assertEquals(before,vm.uiState.value); assertEquals(3,effects.values.size)
        assertEquals(0,local.subscriptions["device"])
    }
    @Test fun cancelledLogoutClearsPendingWithoutMessageOrSessionChange() = runTest(main.testDispatcher) {
        accounts.identity.value=a; val vm=vm(); vm.initialize(); runCurrent()
        val effects=EffectRecorder(vm.effect,backgroundScope)
        accounts.logout={throw CancellationException()}
        vm.onEvent(AccountLibraryEvent.Logout); runCurrent()
        assertTrue(vm.uiState.value.pending.isEmpty()); assertTrue(effects.values.isEmpty())
        assertEquals(a,content(vm).identity)
    }
}
