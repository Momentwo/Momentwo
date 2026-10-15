package cord.eoeo.momentwo.feature.album.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.domain.album.RequestCreateAlbumUseCase
import cord.eoeo.momentwo.core.domain.friend.GetFriendListUseCase
import cord.eoeo.momentwo.core.model.FriendItem
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.album.impl.createalbum.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAlbumViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val repository = AlbumFakeRepository()
    private val friends = AlbumFriendRepository()
    private val handle = SavedStateHandle()
    private val store = ViewModelStore()
    private fun vm() = CreateAlbumViewModel(RequestCreateAlbumUseCase(repository), GetFriendListUseCase(friends), handle).also { store.put("create", it) }
    @After fun clear() { store.clear(); main.testDispatcher.scheduler.runCurrent() }
    @Test fun pendingCreationCapturesInputsAndKeepsNewEditsWithoutClosing() = runTest(main.testDispatcher) {
        val response = CompletableDeferred<Unit>()
        repository.create = { response.await(); Result.success(Unit) }
        val vm = vm()
        vm.onEvent(CreateAlbumEvent.OnTitleChanged("first"))
        vm.onEvent(CreateAlbumEvent.OnRequestCreateAlbum("first")); runCurrent()
        vm.onEvent(CreateAlbumEvent.OnTitleChanged("edited"))
        vm.onEvent(CreateAlbumEvent.OnRequestCreateAlbum("edited"))
        response.complete(Unit); advanceUntilIdle()
        assertEquals(listOf("first" to emptyList<String>()), repository.creations)
        assertEquals("edited", vm.uiState.value.title)
        assertEquals("edited", handle.get<String>("create_album_title"))
        assertTrue(vm.effect.first() is CreateAlbumEffect.ShowSnackbar)
        assertFalse(vm.uiState.value.isLoading)
    }
    @Test fun unchangedSuccessfulFormPopsBack() = runTest(main.testDispatcher) {
        val vm = vm()
        vm.onEvent(CreateAlbumEvent.OnRequestCreateAlbum("fixture")); advanceUntilIdle()
        assertEquals(CreateAlbumEffect.PopBackStack, vm.effect.first())
    }
    @Test fun lateFriendLoadKeepsTemporarySelectionsAndDismissRestoresConfirmedSelection() = runTest(main.testDispatcher) {
        val response = CompletableDeferred<List<FriendItem>>()
        friends.read = { Result.success(response.await()) }
        val vm = vm(); vm.onEvent(CreateAlbumEvent.OnClickInviteFriend); runCurrent()
        val first = FriendItem("first", "")
        vm.onEvent(CreateAlbumEvent.OnCheckedChange(true, first))
        response.complete(listOf(first, FriendItem("second", ""))); advanceUntilIdle()
        assertTrue(vm.uiState.value.isChecked(first))
        vm.onEvent(CreateAlbumEvent.OnConfirmInviteFriend)
        vm.onEvent(CreateAlbumEvent.OnClickInviteFriend)
        vm.onEvent(CreateAlbumEvent.OnCheckedChange(false, first))
        vm.onEvent(CreateAlbumEvent.OnDismissInviteFriend)
        assertEquals(listOf(first), vm.uiState.value.selectedFriendList)
        assertTrue(vm.uiState.value.isChecked(first))
    }
    @Test fun cancelledCreationDoesNotBecomeErrorOrNavigation() = runTest(main.testDispatcher) {
        repository.create = { Result.failure(CancellationException("cancelled")) }
        val vm = vm(); vm.onEvent(CreateAlbumEvent.OnRequestCreateAlbum("fixture")); advanceUntilIdle()
        assertEquals(CreateAlbumStatus.Idle, vm.uiState.value.status)
    }
    @Test fun savedTitleIsUsedAsInitialState() = runTest(main.testDispatcher) {
        handle["create_album_title"] = "restored"
        assertEquals("restored", vm().uiState.value.title)
    }
    @Test fun failedCreationKeepsConfirmedInvitationsAndRetrySendsSamePayloadOnce() = runTest(main.testDispatcher) {
        val vm=vm();val effects=cord.eoeo.momentwo.core.testing.EffectRecorder(vm.effect,backgroundScope)
        val friend=FriendItem("invited","")
        vm.onEvent(CreateAlbumEvent.OnTitleChanged("album"))
        vm.onEvent(CreateAlbumEvent.OnCheckedChange(true,friend));vm.onEvent(CreateAlbumEvent.OnConfirmInviteFriend)
        repository.create={Result.failure(java.io.IOException("offline"))}
        vm.onEvent(CreateAlbumEvent.OnRequestCreateAlbum("album"));runCurrent()
        assertTrue(vm.uiState.value.status is CreateAlbumStatus.Failed)
        assertEquals("album",vm.uiState.value.title);assertEquals(listOf(friend),vm.uiState.value.selectedFriendList)
        assertFalse(effects.values.any{it==CreateAlbumEffect.PopBackStack})
        repository.create={Result.success(Unit)};vm.onEvent(CreateAlbumEvent.OnRequestCreateAlbum("album"));runCurrent()
        assertEquals(listOf("album" to listOf("invited"),"album" to listOf("invited")),repository.creations)
        assertEquals(1,effects.values.count{it==CreateAlbumEffect.PopBackStack})
    }

}
