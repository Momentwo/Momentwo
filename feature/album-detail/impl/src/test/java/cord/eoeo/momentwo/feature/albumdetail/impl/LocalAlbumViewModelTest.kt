package cord.eoeo.momentwo.feature.albumdetail.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.LocalLibraryRepository
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.albumdetail.impl.local.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class LocalAlbumViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val repo = AlbumLocalFake()
    private val drafts = EmptyLibraryDrafts()
    private val store = ViewModelStore()
    private fun vm() = LocalAlbumViewModel(repo, drafts, "album").also { store.put("album", it); it.initialize() }
    @After fun clear() { store.clear(); main.testDispatcher.scheduler.runCurrent() }
    @Test fun missingAlbumIsErrorAndCannotKeepDeletedContent() = runTest(main.testDispatcher) {
        val vm = vm(); runCurrent(); assertTrue(vm.uiState.value.screen is ScreenState.Success)
        repo.album.value = null; runCurrent()
        assertEquals(ScreenState.Error(UiError.NotFound), vm.uiState.value.screen)
    }
    @Test fun snapshotsCannotOverwriteCurrentEditorAndRenameTargetsId() = runTest(main.testDispatcher) {
        val vm = vm(); runCurrent(); vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.RenameSub("child")))
        vm.onEvent(LocalAlbumEvent.Text("draft")); runCurrent()
        repo.subs.value = listOf(LocalSubAlbum("other", "album", "other", null, 0), repo.subs.value.first()); runCurrent()
        assertEquals(LocalAlbumDialog.RenameSub("child"), vm.uiState.value.dialog)
        assertEquals("draft", vm.uiState.value.draft.text)
        assertEquals(2, (vm.uiState.value.screen as ScreenState.Success).content.subAlbums.size)
    }
    @Test fun delayedWriteKeepsNewInputAndIgnoresDuplicateSubmit() = runTest(main.testDispatcher) {
        val gate = CompletableDeferred<Unit>(); repo.edit = { _,_,_ -> gate.await() }
        val vm = vm(); runCurrent(); vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.EditAlbum))
        vm.onEvent(LocalAlbumEvent.Save); runCurrent()
        vm.onEvent(LocalAlbumEvent.Text("later")); vm.onEvent(LocalAlbumEvent.Save); runCurrent()
        gate.complete(Unit); runCurrent()
        assertEquals(1, repo.edits); assertEquals("later", vm.uiState.value.draft.text)
        assertEquals(LocalAlbumDialog.EditAlbum, vm.uiState.value.dialog); assertTrue(vm.uiState.value.pending.isEmpty())
        assertEquals("later", drafts.value!!.text)
    }
    @Test fun bulkDeletionSnapshotsIdsAndDoesNotClearLaterSelection() = runTest(main.testDispatcher) {
        val gate=CompletableDeferred<Unit>();repo.bulk={_,_->gate.await()}
        val vm=vm();runCurrent();vm.onEvent(LocalAlbumEvent.Edit);vm.onEvent(LocalAlbumEvent.Select("child",true))
        vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.DeleteSelected));vm.onEvent(LocalAlbumEvent.ConfirmDelete);runCurrent()
        vm.onEvent(LocalAlbumEvent.Select("new",true));vm.onEvent(LocalAlbumEvent.ConfirmDelete);runCurrent()
        gate.complete(Unit);runCurrent()
        assertEquals(listOf("album" to setOf("child")),repo.deleted)
        assertEquals(setOf("new"),vm.uiState.value.selected);assertTrue(vm.uiState.value.editing)
    }
    @Test fun cancelledBulkRequestPublishesNoResultOrMessage() = runTest(main.testDispatcher) {
        repo.bulk={_,_->awaitCancellation()};val effects=mutableListOf<LocalAlbumEffect>()
        val vm=vm();backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){vm.effect.collect{effects+=it}}
        runCurrent();vm.onEvent(LocalAlbumEvent.Edit);vm.onEvent(LocalAlbumEvent.Select("child",true))
        vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.DeleteSelected));vm.onEvent(LocalAlbumEvent.ConfirmDelete);runCurrent()
        val before=vm.uiState.value;store.clear();runCurrent();assertEquals(before,vm.uiState.value);assertTrue(effects.isEmpty())
    }
    @Test fun savedDraftRestoresItsRoleButDismissCannotBeUndoneBySnapshot() = runTest(main.testDispatcher) {
        drafts.value = LibraryDraft("restored", "extra", "receipt", "edit-album")
        val vm = vm(); runCurrent(); assertEquals(LocalAlbumDialog.EditAlbum, vm.uiState.value.dialog)
        vm.onEvent(LocalAlbumEvent.Dismiss); runCurrent(); repo.album.value = repo.album.value!!.copy(title="new"); runCurrent()
        assertEquals(LocalAlbumDialog.None, vm.uiState.value.dialog); assertNull(drafts.value)
    }
    @Test fun failedWritePreservesEditorAndBackCancelsEditingBeforeNavigation() = runTest(main.testDispatcher) {
        val vm=vm();runCurrent();val effects=cord.eoeo.momentwo.core.testing.EffectRecorder(vm.effect,backgroundScope)
        repo.edit={_,_,_->throw IOException("disk")}
        vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.EditAlbum));vm.onEvent(LocalAlbumEvent.Text("keep"));runCurrent()
        vm.onEvent(LocalAlbumEvent.Save);runCurrent()
        assertEquals("keep",vm.uiState.value.draft.text);assertEquals(LocalAlbumDialog.EditAlbum,vm.uiState.value.dialog)
        assertTrue(vm.uiState.value.pending.isEmpty());assertEquals(1,effects.values.size)
        vm.onEvent(LocalAlbumEvent.Edit);vm.onEvent(LocalAlbumEvent.Back);runCurrent();assertFalse(vm.uiState.value.editing)
        assertEquals(1,effects.values.size)
        vm.onEvent(LocalAlbumEvent.Back);runCurrent();assertEquals(LocalAlbumEffect.Back,effects.values.last())
    }

    @Test fun accountMemberFailureCanRetryAndDeviceInvitationOnlyRequestsConnection() = runTest(main.testDispatcher) {
        var reads=0;var failing=true
        val account=object:StrictAccountLibraryAccess(){
            override fun identities()=flowOf(AccountIdentity("A","one",true))
            override suspend fun refreshAlbum(id:String) {}
            override suspend fun members(id:String):List<MemberItem>{reads++;if(failing)throw IOException("offline");return listOf(MemberItem(7,"seven","",MemberAuth.MEMBER))}
        }
        repo.album.value=repo.album.value!!.copy(libraryId="account:A",remoteId=123,role=MemberAuth.ADMIN.roleString)
        val vm=LocalAlbumViewModel(repo,drafts,"album",account).also{store.put("album",it)}
        val effects=cord.eoeo.momentwo.core.testing.EffectRecorder(vm.effect,backgroundScope)
        vm.initialize();runCurrent();vm.onEvent(LocalAlbumEvent.Tab(1));runCurrent()
        assertEquals(ScreenState.Error(UiError.Network),vm.uiState.value.members);assertTrue(effects.values.isEmpty())
        failing=false;vm.onEvent(LocalAlbumEvent.Retry);runCurrent()
        assertEquals(7,(vm.uiState.value.members as ScreenState.Success).content.single().id)
        assertEquals(2,reads)
        repo.album.value=repo.album.value!!.copy(libraryId="device",remoteId=null,role=null);runCurrent()
        vm.onEvent(LocalAlbumEvent.Add);runCurrent();assertEquals(listOf(LocalAlbumEffect.Connect),effects.values)
        assertEquals(2,reads)
    }

}
private class AlbumLocalFake : LocalLibraryRepository {
    var create: suspend (String, String) -> String = { _, id -> id }
    val created = mutableListOf<String>()
    var starts = 0
    var observed: LibraryScope? = null
    var initialize: suspend () -> Unit = {}
    val rows = MutableStateFlow<List<LocalAlbum>>(emptyList())
    override suspend fun initialize() { starts++; initialize.invoke() }
    override fun albums(scope: LibraryScope): Flow<List<LocalAlbum>> { observed = scope; return rows }
    val album = MutableStateFlow<LocalAlbum?>(LocalAlbum("album", "device", "old", "subtitle", "", null, 0))
    val subs = MutableStateFlow(listOf(LocalSubAlbum("child", "album", "child", null, 0)))
    var edit: suspend (String, String, String) -> Unit = { _, _, _ -> }
    var edits = 0
    override fun album(id: String): Flow<LocalAlbum?> = album
    override fun subAlbums(albumId: String): Flow<List<LocalSubAlbum>> = subs
    override fun subAlbum(id: String): Flow<LocalSubAlbum?> = flowOf(null)
    override fun photos(subAlbumId: String): Flow<List<LocalPhoto>> = flowOf(emptyList())
    override fun photo(id: String): Flow<LocalPhoto?> = flowOf(null)
    override fun memos(photoId: String): Flow<List<LocalMemo>> = flowOf(emptyList())
    override suspend fun createAlbum(title: String, localId: String): String { created += title; return create(title, localId) }
    override suspend fun createSubAlbum(albumId: String, title: String, localId: String): String = error("unused")
    override suspend fun editAlbum(id: String, title: String, subtitle: String) { edits++; edit(id,title,subtitle) }
    override suspend fun editAlbumTitle(id: String, title: String): Unit = error("unused")
    override suspend fun editAlbumSubtitle(id: String, subtitle: String): Unit = error("unused")
    var bulk: suspend (String,Set<String>)->Unit={_,_->}
    val deleted=mutableListOf<Pair<String,Set<String>>>()
    override suspend fun deleteSubAlbums(albumId: String, ids: Set<String>) { deleted+=albumId to ids;bulk(albumId,ids) }
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
