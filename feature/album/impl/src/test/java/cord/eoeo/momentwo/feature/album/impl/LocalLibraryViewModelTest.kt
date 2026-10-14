package cord.eoeo.momentwo.feature.album.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.LocalLibraryRepository
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.album.impl.local.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class LocalLibraryViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val repository = LocalLibraryFake()
    private val store = ViewModelStore()
    private fun vm() = LocalLibraryViewModel(repository).also { store.put("device", it) }
    @After fun clear() = store.clear()
    @Test fun deviceEmptySuccessDoesNotNeedLoginAndInitializationIsIdempotent() = runTest(main.testDispatcher) {
        val vm = vm(); vm.initialize(); runCurrent(); vm.initialize(); runCurrent()
        assertEquals(ScreenState.Success(emptyList<LocalAlbum>()), vm.uiState.value.screen)
        assertEquals(1, repository.starts)
        assertEquals(LibraryScope.Device, repository.observed)
        vm.onEvent(LocalLibraryEvent.ConnectAccount); runCurrent()
        assertEquals(LocalLibraryEffect.Login, vm.effect.first())
    }
    @Test fun localStorageFailureCanBeRetriedWithoutReplacingItWithEmptySuccess() = runTest(main.testDispatcher) {
        repository.initialize = { throw IOException("storage") }
        val vm = vm(); vm.initialize(); runCurrent()
        assertEquals(ScreenState.Error(UiError.Unknown), vm.uiState.value.screen)
        repository.initialize = {}
        vm.onEvent(LocalLibraryEvent.Retry); runCurrent()
        assertEquals(ScreenState.Success(emptyList<LocalAlbum>()), vm.uiState.value.screen)
    }
    @Test fun clearedViewModelCannotReceiveLateLocalUpdates() = runTest(main.testDispatcher) {
        val vm = vm(); vm.initialize(); runCurrent(); store.clear()
        val before = vm.uiState.value
        repository.rows.value = listOf(LocalAlbum("local", "device", "late", "", "", null, 0))
        runCurrent(); assertEquals(before, vm.uiState.value)
    }
}
private class LocalLibraryFake : LocalLibraryRepository {
    var starts = 0
    var observed: LibraryScope? = null
    var initialize: suspend () -> Unit = {}
    val rows = MutableStateFlow<List<LocalAlbum>>(emptyList())
    override suspend fun initialize() { starts++; initialize.invoke() }
    override fun albums(scope: LibraryScope): Flow<List<LocalAlbum>> { observed = scope; return rows }
    override fun album(id: String): Flow<LocalAlbum?> = flowOf(null)
    override fun subAlbums(albumId: String): Flow<List<LocalSubAlbum>> = flowOf(emptyList())
    override fun subAlbum(id: String): Flow<LocalSubAlbum?> = flowOf(null)
    override fun photos(subAlbumId: String): Flow<List<LocalPhoto>> = flowOf(emptyList())
    override fun photo(id: String): Flow<LocalPhoto?> = flowOf(null)
    override fun memos(photoId: String): Flow<List<LocalMemo>> = flowOf(emptyList())
    override suspend fun createAlbum(title: String): String = error("unused")
    override suspend fun createSubAlbum(albumId: String, title: String): String = error("unused")
    override suspend fun editAlbum(id: String, title: String, subtitle: String): Unit = error("unused")
    override suspend fun editSubAlbum(id: String, title: String): Unit = error("unused")
    override suspend fun deleteAlbum(id: String): Unit = error("unused")
    override suspend fun deleteSubAlbum(id: String): Unit = error("unused")
}
