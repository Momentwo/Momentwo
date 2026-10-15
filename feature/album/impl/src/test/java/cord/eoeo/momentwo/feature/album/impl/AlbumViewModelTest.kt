package cord.eoeo.momentwo.feature.album.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.domain.album.GetAlbumListUseCase
import cord.eoeo.momentwo.core.model.AlbumItem
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.album.impl.album.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val repository = AlbumFakeRepository()
    private val store = ViewModelStore()
    private fun vm() = AlbumViewModel(GetAlbumListUseCase(repository)).also { store.put("album", it) }
    @After fun clear() { store.clear() }
    @Test fun initialEmptySuccessAndInitializationAreDistinctFromLoading() = runTest(main.testDispatcher) {
        val vm = vm(); assertEquals(ScreenState.Loading, vm.uiState.value.screen)
        vm.initialize(); advanceUntilIdle(); vm.initialize(); advanceUntilIdle()
        assertEquals(ScreenState.Success(AlbumContent(emptyList())), vm.uiState.value.screen)
        assertEquals(1, repository.reads)
    }
    @Test fun refreshFailureKeepsExistingAlbums() = runTest(main.testDispatcher) {
        val item = AlbumItem(1, "fixture", "", "", 2)
        repository.read = { Result.success(listOf(item)) }
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        repository.read = { Result.failure(IOException("offline")) }
        vm.onEvent(AlbumEvent.OnGetAlbumList); advanceUntilIdle()
        val success = vm.uiState.value.screen as ScreenState.Success
        assertEquals(listOf(item), success.content.albums)
        assertEquals(RefreshState.Error(UiError.Network), success.refresh)
    }
    @Test fun latestRequestWinsAndChildVisitCausesOnlyOneReload() = runTest(main.testDispatcher) {
        val response = CompletableDeferred<List<AlbumItem>>()
        repository.read = { Result.success(response.await()) }
        val vm = vm(); vm.initialize(); runCurrent()
        vm.onEvent(AlbumEvent.OnInvalidate)
        repository.read = { Result.success(emptyList()) }
        vm.initialize(); runCurrent(); response.complete(listOf(AlbumItem(1, "old", "", "", 0)))
        advanceUntilIdle(); vm.initialize(); advanceUntilIdle()
        assertEquals(2, repository.reads)
        assertEquals(emptyList<AlbumItem>(), (vm.uiState.value.screen as ScreenState.Success).content.albums)
    }
    @Test fun initialFailureIsErrorAndCancellationIsNotError() = runTest(main.testDispatcher) {
        repository.read = { Result.failure(IOException("offline")) }
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        assertEquals(ScreenState.Error(UiError.Network), vm.uiState.value.screen)
        repository.read = { Result.failure(CancellationException("cancelled")) }
        vm.onEvent(AlbumEvent.OnGetAlbumList); advanceUntilIdle()
        assertEquals(ScreenState.Loading, vm.uiState.value.screen)
    }
    @Test fun initialErrorRetrySucceedsWithoutAnErrorEffect() = runTest(main.testDispatcher) {
        repository.read={Result.failure(IOException("first"))};val vm=vm()
        val effects=cord.eoeo.momentwo.core.testing.EffectRecorder(vm.effect,backgroundScope)
        vm.initialize();runCurrent();assertTrue(vm.uiState.value.screen is ScreenState.Error)
        repository.read={Result.success(emptyList())};vm.onEvent(AlbumEvent.OnGetAlbumList);runCurrent()
        assertEquals(ScreenState.Success(AlbumContent(emptyList())),vm.uiState.value.screen)
        assertTrue(effects.values.isEmpty())
    }

}
