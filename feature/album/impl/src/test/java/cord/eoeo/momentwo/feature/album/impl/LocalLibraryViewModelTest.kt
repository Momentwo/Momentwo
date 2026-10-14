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
    private val drafts = EmptyLibraryDrafts()
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

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryCreateDeviceViewModelTest {
    @get:Rule val main=MainDispatcherRule(StandardTestDispatcher())
    private val repository=LocalLibraryFake();private val drafts=EmptyLibraryDrafts();private val store=ViewModelStore()
    private fun vm()=LibraryCreateViewModel(repository,drafts).also{store.put("create",it)}
    @After fun clear()=store.clear()
    @Test fun submissionSnapshotsInputSkipsDuplicatesAndKeepsNewDraft() = runTest(main.testDispatcher) {
        val response = CompletableDeferred<Unit>()
        repository.create = { _, id -> response.await(); id }
        val vm = vm(); vm.initialize(); runCurrent()
        vm.onEvent(LibraryCreateEvent.Text("submitted")); runCurrent()
        vm.onEvent(LibraryCreateEvent.Submit); runCurrent()
        vm.onEvent(LibraryCreateEvent.Text("new draft")); vm.onEvent(LibraryCreateEvent.Submit); runCurrent()
        response.complete(Unit); runCurrent()
        assertEquals(listOf("submitted"), repository.created)
        assertEquals("new draft", vm.uiState.value.draft.text)
        assertTrue(vm.uiState.value.ready)
        assertFalse(vm.uiState.value.pending)
        assertTrue(vm.effect.first() is LibraryCreateEffect.Message)
        assertEquals("new draft", drafts.value!!.text)
    }
    @Test fun restoredDraftIsOnlyClearedWhenItsOwnSubmissionCompletes() = runTest(main.testDispatcher) {
        drafts.value = LibraryDraft("restored", "", "receipt")
        val vm = vm(); vm.initialize(); runCurrent()
        assertTrue(vm.uiState.value.ready)
        vm.onEvent(LibraryCreateEvent.Submit); runCurrent()
        assertEquals("",vm.uiState.value.draft.text)
        assertNull(drafts.value)
        assertEquals(LibraryCreateEffect.Created(false), vm.effect.first())
    }
    @Test fun cancellingViewModelDoesNotPublishCreateResultOrFailureEffect() = runTest(main.testDispatcher) {
        repository.create = { _, _ -> awaitCancellation() }
        val effects = mutableListOf<LibraryCreateEffect>()
        val vm = vm(); backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effect.collect { effects += it } }
        vm.initialize(); runCurrent(); vm.onEvent(LibraryCreateEvent.Text("pending")); runCurrent()
        vm.onEvent(LibraryCreateEvent.Submit); runCurrent(); val before = vm.uiState.value
        store.clear(); runCurrent()
        assertEquals(before, vm.uiState.value)
        assertTrue(effects.isEmpty())
    }
    @Test fun failedDraftRestoreCanRetryWithoutReplacingTheSavedDraft()=runTest(main.testDispatcher) {
        drafts.failLoad=true;drafts.value=LibraryDraft("saved","","receipt")
        val vm=vm();vm.initialize();runCurrent();assertTrue(vm.uiState.value.restoreFailed);assertFalse(vm.uiState.value.ready)
        drafts.failLoad=false;vm.onEvent(LibraryCreateEvent.Retry);runCurrent()
        assertTrue(vm.uiState.value.ready);assertEquals("saved",vm.uiState.value.draft.text)
    }
}
private class LocalLibraryFake : LocalLibraryRepository {
    var create: suspend (String, String) -> String = { _, id -> id }
    val created = mutableListOf<String>()
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
    override suspend fun createAlbum(title: String, localId: String): String { created += title; return create(title, localId) }
    override suspend fun createSubAlbum(albumId: String, title: String, localId: String): String = error("unused")
    override suspend fun editAlbum(id: String, title: String, subtitle: String): Unit = error("unused")
    override suspend fun editAlbumTitle(id: String, title: String): Unit = error("unused")
    override suspend fun editAlbumSubtitle(id: String, subtitle: String): Unit = error("unused")
    override suspend fun deleteSubAlbums(albumId: String, ids: Set<String>): Unit = error("unused")
    override suspend fun deletePhotos(subAlbumId: String, ids: Set<String>): Unit = error("unused")
    override suspend fun editSubAlbum(id: String, title: String): Unit = error("unused")
    override suspend fun setCover(id: String, source: android.net.Uri?): Unit = error("unused")
    override suspend fun importPhoto(subAlbumId: String, source: android.net.Uri, localId: String): String = error("unused")
    override suspend fun setDescription(id: String, text: String): Unit = error("unused")
    override suspend fun setFavorite(id: String, value: Boolean): Unit = error("unused")
    override suspend fun saveMemo(photoId: String, text: String, memoId: String): String = error("unused")
    override suspend fun deleteMemo(id: String): Unit = error("unused")
    override suspend fun deletePhoto(id: String): Unit = error("unused")
    override suspend fun exportPhoto(id: String): Unit = error("unused")
    override suspend fun cleanUnusedMedia() {}
    override suspend fun deleteAlbum(id: String): Unit = error("unused")
    override suspend fun deleteSubAlbum(id: String): Unit = error("unused")
}

private class EmptyLibraryDrafts : cord.eoeo.momentwo.core.data.library.LibraryDrafts {
    var value: LibraryDraft? = null
    var failLoad = false
    override suspend fun load(key: String): LibraryDraft? { if (failLoad) throw IOException("draft storage"); return value }
    override suspend fun save(key: String, draft: LibraryDraft) { value = draft }
    override suspend fun clearIfMatches(key: String, token: String) { if (value?.token == token) value = null }
}
