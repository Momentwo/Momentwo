package cord.eoeo.momentwo.feature.photo.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.LocalLibraryRepository
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.photo.impl.local.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class LocalPhotoViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val repo = PhotoLocalFake(); private val drafts = EmptyLibraryDrafts(); private val store = ViewModelStore()
    private fun vm() = LocalPhotoViewModel(repo,drafts,"photo").also {store.put("photo",it);it.initialize()}
    @After fun clear() = store.clear()
    @Test fun knownParentTitlesRemainWhileFirstPageIsUnavailableButDisappearOnOwnershipLoss()=runTest(main.testDispatcher) {
        val vm=LocalPhotoListViewModel(repo,"child");store.put("list",vm);vm.initialize();runCurrent()
        assertEquals(ScreenState.Loading,vm.uiState.value.screen);assertEquals(LocalPhotoListHeader("old","child"),vm.uiState.value.header)
        repo.album.value=null;runCurrent();assertEquals(ScreenState.Error(UiError.NotFound),vm.uiState.value.screen);assertNull(vm.uiState.value.header)
    }
    @Test fun captionCanBeClearedWithoutOverwritingFavoriteOrMemo() = runTest(main.testDispatcher) {
        repo.description = {repo.photo.value=repo.photo.value!!.copy(description=it)}
        val vm=vm();runCurrent();vm.onEvent(LocalPhotoEvent.Favorite);runCurrent()
        vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Caption));vm.onEvent(LocalPhotoEvent.Text(""));vm.onEvent(LocalPhotoEvent.Save);runCurrent()
        val content=(vm.uiState.value.screen as ScreenState.Success).content
        assertEquals("",content.photo.description);assertTrue(content.photo.favorite);assertTrue(content.memos.isEmpty())
    }
    @Test fun slowCaptionCannotEraseLatestInputAndFailureKeepsContent() = runTest(main.testDispatcher) {
        val gate=CompletableDeferred<Unit>();repo.description={gate.await();throw IOException("full")}
        val vm=vm();runCurrent();vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Caption));vm.onEvent(LocalPhotoEvent.Text("submitted"));vm.onEvent(LocalPhotoEvent.Save);runCurrent()
        vm.onEvent(LocalPhotoEvent.Text("new draft"));runCurrent();gate.complete(Unit);runCurrent()
        assertEquals("new draft",vm.uiState.value.draft.text);assertEquals("old",(vm.uiState.value.screen as ScreenState.Success).content.photo.description)
        assertTrue(vm.uiState.value.pending.isEmpty());assertTrue(vm.effect.first() is LocalPhotoEffect.Message)
    }
    @Test fun restoredPersonalMemoSavesWithItsOwnIntentAndNeverNeedsAuthorOrApi() = runTest(main.testDispatcher) {
        drafts.value=LibraryDraft("private","","intent","memo:new")
        val vm=vm();runCurrent();vm.onEvent(LocalPhotoEvent.Save);runCurrent()
        assertEquals(LocalMemo("intent","photo","private",0),repo.notes.value.single());assertNull(drafts.value)
        assertEquals(LocalPhotoDialog.None,vm.uiState.value.dialog)
    }
    @Test fun deletedPhotoClearsContentAndViewModelCancellationHasNoLateResult() = runTest(main.testDispatcher) {
        repo.description={awaitCancellation()};val vm=vm();runCurrent();vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Caption));vm.onEvent(LocalPhotoEvent.Save);runCurrent()
        store.clear();val before=vm.uiState.value;repo.photo.value=null;runCurrent();assertEquals(before,vm.uiState.value)
        val other=vm();runCurrent();assertEquals(ScreenState.Error(UiError.NotFound),other.uiState.value.screen)
    }
    @Test fun exportFailureAndCancellationPreserveContentAndImmersiveState()=runTest(main.testDispatcher){
        val vm=vm();runCurrent();val effects=cord.eoeo.momentwo.core.testing.EffectRecorder(vm.effect,backgroundScope)
        repo.export={throw IOException("gallery")};vm.onEvent(LocalPhotoEvent.Export);runCurrent()
        assertEquals(1,effects.values.size);assertTrue(vm.uiState.value.screen is ScreenState.Success)
        repo.export={throw CancellationException()};vm.onEvent(LocalPhotoEvent.Export);runCurrent();assertEquals(1,effects.values.size)
        vm.onEvent(LocalPhotoEvent.ToggleImmersive);val bars=vm.uiState.value
        repo.photo.value=repo.photo.value!!.copy(favorite=true);runCurrent()
        assertEquals(bars.immersive,vm.uiState.value.immersive)
    }

    @Test fun accountLikeAndCommentStaySeparateFromPersonalFavoriteAndMemo()=runTest(main.testDispatcher){
        val owner=AccountIdentity("A","one",true)
        repo.photo.value=repo.photo.value!!.copy(libraryId="account:A",role=MemberAuth.ADMIN.roleString)
        val account=object:StrictAccountLibraryAccess(){
            override fun identities()=flowOf(owner)
            override suspend fun refreshPhoto(id:String) {}
        }
        val calls=mutableListOf<String>()
        val sync=object:StrictAccountSyncOperations(){
            override fun comments(photoId:String)=flowOf(emptyList<AccountComment>())
            override suspend fun setLiked(photoId:String,liked:Boolean){calls+="like:$photoId:$liked"}
            override suspend fun saveComment(photoId:String,text:String,id:String){calls+="comment:$photoId:$text"}
        }
        val vm=LocalPhotoViewModel(repo,drafts,"photo",account,sync).also{store.put("photo",it);it.initialize()};runCurrent()
        vm.onEvent(LocalPhotoEvent.Favorite);runCurrent();assertTrue(calls.isEmpty())
        vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Memo(null)));vm.onEvent(LocalPhotoEvent.Text("private"));runCurrent()
        vm.onEvent(LocalPhotoEvent.Save);runCurrent();assertTrue(calls.isEmpty());assertEquals("private",repo.notes.value.single().text)
        vm.onEvent(LocalPhotoEvent.Like);runCurrent()
        vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Comment(null)));vm.onEvent(LocalPhotoEvent.Text("shared"));runCurrent()
        vm.onEvent(LocalPhotoEvent.Save);runCurrent()
        assertEquals(listOf("like:photo:true","comment:photo:shared"),calls)
        assertEquals("private",repo.notes.value.single().text);assertTrue(repo.photo.value!!.favorite)
    }

}
internal class PhotoLocalFake : LocalLibraryRepository {
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
    override fun subAlbum(id: String): Flow<LocalSubAlbum?> = flowOf(LocalSubAlbum("child","album","child",null,0,photosLoaded=false))
    override fun photos(subAlbumId: String): Flow<List<LocalPhoto>> = flowOf(emptyList())
    val photo = MutableStateFlow<LocalPhoto?>(LocalPhoto("photo","child","file:///photo.png","photo.png",null,"old",true,false,false,null,0))
    val notes = MutableStateFlow<List<LocalMemo>>(emptyList())
    var description: suspend (String) -> Unit = {}
    override fun photo(id: String): Flow<LocalPhoto?> = photo
    override fun memos(photoId: String): Flow<List<LocalMemo>> = notes
    override suspend fun createAlbum(title: String, localId: String): String { created += title; return create(title, localId) }
    override suspend fun createSubAlbum(albumId: String, title: String, localId: String): String = error("unused")
    override suspend fun editAlbum(id: String, title: String, subtitle: String) { edits++; edit(id,title,subtitle) }
    override suspend fun editAlbumTitle(id: String, title: String): Unit = error("unused")
    override suspend fun editAlbumSubtitle(id: String, subtitle: String): Unit = error("unused")
    override suspend fun deleteSubAlbums(albumId: String, ids: Set<String>): Unit = error("unused")
    override suspend fun deletePhotos(subAlbumId: String, ids: Set<String>): Unit = error("unused")
    override suspend fun editSubAlbum(id: String, title: String): Unit = error("unused")
    override suspend fun setCover(id: String, source: android.net.Uri?): Unit = error("unused")
    override suspend fun importPhoto(subAlbumId: String, source: android.net.Uri, localId: String): String = error("unused")
    override suspend fun setDescription(id: String, text: String) { description(text) }
    override suspend fun setFavorite(id: String, value: Boolean) { photo.value = photo.value!!.copy(favorite=value) }
    override suspend fun saveMemo(photoId: String, text: String, memoId: String): String { notes.value = notes.value + LocalMemo(memoId,photoId,text,0); return memoId }
    override suspend fun deleteMemo(id: String): Unit = error("unused")
    override suspend fun deletePhoto(id: String): Unit = error("unused")
    var export: suspend () -> Unit = {}
    override suspend fun exportPhoto(id: String) { export() }
    override suspend fun cleanUnusedMedia() {}
    override suspend fun deleteAlbum(id: String): Unit = error("unused")
    override suspend fun deleteSubAlbum(id: String): Unit = error("unused")
}

internal class EmptyLibraryDrafts : cord.eoeo.momentwo.core.data.library.LibraryDrafts {
    var value: LibraryDraft? = null
    var failLoad = false
    override suspend fun load(key: String): LibraryDraft? { if (failLoad) throw IOException("draft storage"); return value }
    override suspend fun save(key: String, draft: LibraryDraft) { value = draft }
    override suspend fun clearIfMatches(key: String, token: String) { if (value?.token == token) value = null }
}
