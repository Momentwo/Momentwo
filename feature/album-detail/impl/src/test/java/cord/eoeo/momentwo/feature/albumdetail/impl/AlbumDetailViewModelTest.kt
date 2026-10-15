package cord.eoeo.momentwo.feature.albumdetail.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.domain.friend.GetFriendListUseCase
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val store = ViewModelStore()
    private val album = DetailAlbumRepository()
    private val sub = DetailSubAlbumRepository()
    private val member = DetailMemberRepository()
    private fun vm() = AlbumDetailViewModel(GetFriendListUseCase(DetailFriendRepository()), sub, member, album,
        AlbumDetailState(AlbumHeader(123, "fixture", "subtitle", ""))).also { store.put("detail", it) }
    @After fun clear() { store.clear(); main.testDispatcher.scheduler.runCurrent() }

    @Test fun successWaitsForPermissionAndListTogether() = runTest(main.testDispatcher) {
        val role = CompletableDeferred<MemberAuth>()
        album.role = { Result.success(role.await()) }
        val vm = vm(); vm.initialize(); runCurrent()
        assertEquals(123, vm.uiState.value.albumItem.id)
        assertEquals(ScreenState.Loading, vm.uiState.value.subAlbums)
        assertNull(vm.uiState.value.permission)
        role.complete(MemberAuth.ADMIN); advanceUntilIdle()
        assertEquals(MemberAuth.ADMIN, vm.uiState.value.permission)
        assertEquals(2, (vm.uiState.value.subAlbums as ScreenState.Success).content.items.size)
    }
    @Test fun partialFailureIsWholeTabErrorWithoutDefaultAdmin() = runTest(main.testDispatcher) {
        sub.read = { Result.failure(IOException("offline")) }
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        assertEquals(ScreenState.Error(UiError.Network), vm.uiState.value.subAlbums)
        assertNull(vm.uiState.value.permission)
    }
    @Test fun responseFromOldTabCannotReplaceActiveTab() = runTest(main.testDispatcher) {
        val first = CompletableDeferred<List<SubAlbumItem>>()
        sub.read = { Result.success(first.await()) }
        val vm = vm(); vm.initialize(); runCurrent()
        vm.onEvent(AlbumDetailEvent.OnChangeNavIndex(1)); runCurrent()
        assertTrue(vm.uiState.value.members is ScreenState.Success)
        first.complete(sub.items); advanceUntilIdle()
        assertEquals(DetailTab.Members, vm.uiState.value.tab)
        assertTrue(vm.uiState.value.screen is ScreenState.Success)
    }
    @Test fun deletionDoesNotClearNewSelectionOrSubmitTwice() = runTest(main.testDispatcher) {
        val deleted = CompletableDeferred<Unit>()
        sub.delete = { deleted.await(); Result.success(Unit) }
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        vm.onEvent(AlbumDetailEvent.OnClickEdit)
        vm.onEvent(AlbumDetailEvent.OnChangeSubAlbumSelected(true, 1))
        vm.onEvent(AlbumDetailEvent.OnConfirmEdit); runCurrent()
        vm.onEvent(AlbumDetailEvent.OnChangeSubAlbumSelected(true, 2))
        vm.onEvent(AlbumDetailEvent.OnConfirmEdit)
        deleted.complete(Unit); advanceUntilIdle()
        assertEquals(listOf(listOf(1)), sub.deletions)
        assertEquals(listOf(2), vm.uiState.value.selectedSubAlbumIds)
        assertTrue(vm.uiState.value.isEditMode)
        assertTrue(vm.uiState.value.pendingWrites.isEmpty())
    }
    @Test fun invitationKeepsNewlyAddedFriendAndDialog() = runTest(main.testDispatcher) {
        val invited = CompletableDeferred<Unit>()
        member.invite = { invited.await(); Result.success(Unit) }
        val vm = vm()
        vm.onEvent(AlbumDetailEvent.OnChangeNavIndex(1)); advanceUntilIdle()
        vm.onEvent(AlbumDetailEvent.OnClickInvite); advanceUntilIdle()
        vm.onEvent(AlbumDetailEvent.OnChangeFriendSelected(true, FriendItem("first", "")))
        vm.onEvent(AlbumDetailEvent.OnConfirmInviteFriend); runCurrent()
        vm.onEvent(AlbumDetailEvent.OnChangeFriendSelected(true, FriendItem("second", "")))
        vm.onEvent(AlbumDetailEvent.OnConfirmInviteFriend)
        invited.complete(Unit); advanceUntilIdle()
        assertEquals(listOf(listOf("first")), member.invitations)
        assertEquals(listOf("second"), vm.uiState.value.selectedFriendList.map { it.nickname })
        assertTrue(vm.uiState.value.isInviteDialogOpened)
    }
    @Test fun cancellationIsNotTabError() = runTest(main.testDispatcher) {
        sub.read = { Result.failure(CancellationException("cancelled")) }
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        assertEquals(ScreenState.Loading, vm.uiState.value.subAlbums)
        assertNull(vm.uiState.value.permission)
    }
    @Test fun writesAreBlockedBeforeRequiredPermissionIsLoaded() = runTest(main.testDispatcher) {
        val vm = vm()
        vm.onEvent(AlbumDetailEvent.OnSubAlbumEvents(SubAlbumEvent.Create("new")))
        advanceUntilIdle()
        assertEquals(0, sub.creates)
    }

    @Test fun childNavigationInvalidatesWithoutRepeatingInitialLoad() = runTest(main.testDispatcher) {
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        vm.initialize()
        vm.onEvent(AlbumDetailEvent.OnSubAlbumEvents(SubAlbumEvent.GetSubAlbums)); advanceUntilIdle()
        assertEquals(1, sub.reads)
        vm.onEvent(AlbumDetailEvent.OnSubAlbumVisited)
        vm.onEvent(AlbumDetailEvent.OnSubAlbumEvents(SubAlbumEvent.GetSubAlbums)); advanceUntilIdle()
        assertEquals(2, sub.reads)
        assertFalse(vm.uiState.value.subAlbumsDirty)
    }
    @Test fun tabErrorRetryAndRefreshFailureKeepExistingContentWithoutSnackbar() = runTest(main.testDispatcher) {
        val effects=mutableListOf<AlbumDetailEffect>();val vm=vm()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){vm.effect.collect{effects+=it}}
        sub.read={Result.failure(IOException("first"))};vm.initialize();runCurrent()
        sub.read={Result.success(sub.items)};vm.onEvent(AlbumDetailEvent.OnRetryTab(DetailTab.SubAlbums));runCurrent()
        vm.onEvent(AlbumDetailEvent.OnClickEdit);vm.onEvent(AlbumDetailEvent.OnChangeSubAlbumSelected(true,1))
        sub.read={Result.failure(IOException("refresh"))};vm.onEvent(AlbumDetailEvent.OnRetryTab(DetailTab.SubAlbums));runCurrent()
        assertEquals(RefreshState.Error(UiError.Network),(vm.uiState.value.subAlbums as ScreenState.Success).refresh)
        assertEquals(listOf(1),vm.uiState.value.selectedSubAlbumIds);assertTrue(effects.isEmpty())
        vm.onEvent(AlbumDetailEvent.OnCancelEdit);assertFalse(vm.uiState.value.isEditMode)
        vm.onEvent(AlbumDetailEvent.OnBack);runCurrent();assertEquals(listOf(AlbumDetailEffect.PopBackStack),effects)
    }
    @Test fun failedSettingsAndMemberOperationsNeverNavigateOrClearPendingData() = runTest(main.testDispatcher) {
        val vm=vm();vm.initialize();runCurrent()
        val effects=cord.eoeo.momentwo.core.testing.EffectRecorder(vm.effect,backgroundScope)
        album.operation={Result.failure(IOException("write"))};album.title={Result.failure(IOException("title"))}
        member.operation={Result.failure(IOException("member"))};sub.operation={Result.failure(IOException("sub"))}
        val events=listOf(
            AlbumDetailEvent.OnSubAlbumEvents(SubAlbumEvent.Create("new")),
            AlbumDetailEvent.OnSubAlbumEvents(SubAlbumEvent.EditTitle(1,"new")),
            AlbumDetailEvent.OnAlbumSettingEvents(AlbumSettingEvent.EditTitle("new")),
            AlbumDetailEvent.OnAlbumSettingEvents(AlbumSettingEvent.EditSubTitle("new")),
            AlbumDetailEvent.OnAlbumSettingEvents(AlbumSettingEvent.DeleteSubTitle),
            AlbumDetailEvent.OnAlbumSettingEvents(AlbumSettingEvent.ChangeImage),
            AlbumDetailEvent.OnAlbumSettingEvents(AlbumSettingEvent.DeleteAlbum),
            AlbumDetailEvent.OnMemberEvents(MemberEvent.Exit),
            AlbumDetailEvent.OnMemberEvents(MemberEvent.KickMembers(listOf(7))))
        events.forEach{vm.onEvent(it);runCurrent();assertTrue(vm.uiState.value.pendingWrites.isEmpty())}
        assertEquals(events.size,effects.values.size);assertTrue(effects.values.all{it is AlbumDetailEffect.ShowSnackbar})
        assertEquals("fixture",vm.uiState.value.albumItem.title)
    }
    @Test fun successfulCreationFollowedByReadFailureIsCommittedAndKeepsTabContent() = runTest(main.testDispatcher) {
        val vm=vm();vm.initialize();runCurrent()
        val effects=cord.eoeo.momentwo.core.testing.EffectRecorder(vm.effect,backgroundScope)
        sub.read={Result.failure(IOException("reload"))}
        vm.onEvent(AlbumDetailEvent.OnSubAlbumEvents(SubAlbumEvent.Create("created")));runCurrent()
        assertEquals(1,sub.creates);assertTrue(vm.uiState.value.subAlbums is ScreenState.Success)
        assertEquals(RefreshState.Error(UiError.Network),(vm.uiState.value.subAlbums as ScreenState.Success).refresh)
        assertTrue(effects.values.isEmpty());assertTrue(vm.uiState.value.pendingWrites.isEmpty())
    }
    @Test fun successfulExitAndDeletionEmitOneBackEffectEach() = runTest(main.testDispatcher) {
        val vm=vm();vm.initialize();runCurrent()
        val effects=cord.eoeo.momentwo.core.testing.EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(AlbumDetailEvent.OnMemberEvents(MemberEvent.Exit));runCurrent()
        vm.onEvent(AlbumDetailEvent.OnAlbumSettingEvents(AlbumSettingEvent.DeleteAlbum));runCurrent()
        assertEquals(listOf(AlbumDetailEffect.PopBackStack,AlbumDetailEffect.PopBackStack),effects.values)
    }

}
