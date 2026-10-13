package cord.eoeo.momentwo.feature.photo.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.CommittedWriteException
import cord.eoeo.momentwo.core.domain.subalbum.ChangeSubAlbumTitleUseCase
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.photo.impl.photolist.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoListViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val repository = PhotoFakeRepository()
    private val sub = PhotoFakeSubAlbum()
    private val store = ViewModelStore()
    private fun vm() = PhotoListViewModel(repository, ChangeSubAlbumTitleUseCase(sub), PhotoListState(1, 2, "album", "sub")).also { store.put("list", it) }
    @After fun clear() { store.clear() }
    @Test fun initializationAndReturnRefreshDoNotRecreatePagingSource() = runTest(main.testDispatcher) {
        val vm = vm(); vm.initialize(); advanceUntilIdle(); vm.initialize(); advanceUntilIdle()
        vm.onEvent(PhotoListEvent.OnPhotoVisited); vm.initialize(); advanceUntilIdle()
        assertEquals(1, repository.sourceCalls)
        assertEquals(PhotoListEffect.RefreshPagingData, vm.effect.first())
    }
    @Test fun deletionCapturesIdsAndKeepsNewSelection() = runTest(main.testDispatcher) {
        val response = CompletableDeferred<Unit>()
        repository.delete = { response.await(); Result.success(Unit) }
        val vm = vm()
        vm.onEvent(PhotoListEvent.OnChangeIsEditMode(true))
        vm.onEvent(PhotoListEvent.OnChangeIsSelected(true, 1))
        vm.onEvent(PhotoListEvent.OnClickConfirmEdit); runCurrent()
        vm.onEvent(PhotoListEvent.OnChangeIsSelected(true, 2))
        vm.onEvent(PhotoListEvent.OnClickConfirmEdit)
        response.complete(Unit); advanceUntilIdle()
        assertEquals(listOf(listOf(1)), repository.deleted)
        assertEquals(listOf(2), vm.uiState.value.selectedPhotoIds)
        assertTrue(vm.uiState.value.isEditMode)
        assertTrue(vm.uiState.value.pendingWrites.isEmpty())
    }
    @Test fun renameCannotCloseReopenedDialog() = runTest(main.testDispatcher) {
        val response = CompletableDeferred<Unit>()
        sub.rename = { response.await(); Result.success(Unit) }
        val vm = vm()
        vm.onEvent(PhotoListEvent.OnChangeIsDialogOpened(true))
        vm.onEvent(PhotoListEvent.OnConfirmDialog("submitted")); runCurrent()
        vm.onEvent(PhotoListEvent.OnRenameDraftChanged)
        response.complete(Unit); advanceUntilIdle()
        assertEquals("submitted", vm.uiState.value.subAlbumTitle)
        assertTrue(vm.uiState.value.isDialogOpened)
    }
    @Test fun committedDeletionIsNotRolledBackOnLocalFailure() = runTest(main.testDispatcher) {
        repository.delete = { Result.failure(CommittedWriteException(IOException("local storage"))) }
        val vm = vm()
        vm.onEvent(PhotoListEvent.OnChangeIsSelected(true, 1))
        vm.onEvent(PhotoListEvent.OnClickConfirmEdit); advanceUntilIdle()
        assertTrue(vm.uiState.value.selectedPhotoIds.isEmpty())
        assertEquals(PhotoListEffect.RefreshPagingData, vm.effect.first())
        assertTrue(vm.effect.first() is PhotoListEffect.ShowSnackbar)
    }
    @Test fun cancelledDeletionKeepsSelectionAndClearsOnlyPending() = runTest(main.testDispatcher) {
        repository.delete = { Result.failure(CancellationException("cancelled")) }
        val vm = vm()
        vm.onEvent(PhotoListEvent.OnChangeIsSelected(true, 1))
        vm.onEvent(PhotoListEvent.OnClickConfirmEdit); advanceUntilIdle()
        assertEquals(listOf(1), vm.uiState.value.selectedPhotoIds)
        assertTrue(vm.uiState.value.pendingWrites.isEmpty())
    }
}
