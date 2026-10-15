package cord.eoeo.momentwo.feature.photo.impl
import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.LocalLibraryRepository
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.testing.*
import cord.eoeo.momentwo.feature.photo.impl.local.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class LocalPhotoListViewModelTest {
    @get:Rule val main=MainDispatcherRule(StandardTestDispatcher())
    private val store=ViewModelStore();private val core=PhotoLocalFake();private val drafts=EmptyLibraryDrafts()
    private val sub=MutableStateFlow<LocalSubAlbum?>(LocalSubAlbum("child","album","sub",null,0))
    private var delete:suspend ()->Unit={};private val deleted=mutableListOf<Pair<String,Set<String>>>()
    private val repo=object:LocalLibraryRepository by core{
        override fun subAlbum(id:String)=sub
        override suspend fun deletePhotos(subAlbumId:String,ids:Set<String>){deleted+=subAlbumId to ids.toSet();delete()}
        override suspend fun editSubAlbum(id:String,title:String){delete()}
    }
    private fun vm()=LocalPhotoListViewModel(repo,"child",drafts=drafts).also{store.put("list",it);it.initialize()}
    @After fun clear() { store.clear(); main.testDispatcher.scheduler.runCurrent() }
    @Test fun emptySuccessIsAvailableAndBackCancelsSelectionBeforeNavigating()=runTest(main.testDispatcher){
        val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        assertTrue(vm.uiState.value.screen is ScreenState.Success)
        vm.onEvent(LocalPhotoListEvent.Edit(true));vm.onEvent(LocalPhotoListEvent.Select("one",true))
        vm.onEvent(LocalPhotoListEvent.Back);runCurrent();assertFalse(vm.uiState.value.editing)
        assertTrue(vm.uiState.value.selected.isEmpty());assertTrue(effects.values.isEmpty())
        vm.onEvent(LocalPhotoListEvent.Back);runCurrent();assertEquals(listOf(LocalPhotoListEffect.Back),effects.values)
    }
    @Test fun failedBulkDeleteKeepsSelectionAndSuccessfulRetryPreservesNewSelection()=runTest(main.testDispatcher){
        val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(LocalPhotoListEvent.Edit(true));vm.onEvent(LocalPhotoListEvent.Select("one",true))
        delete={throw IOException("disk")};vm.onEvent(LocalPhotoListEvent.Delete);vm.onEvent(LocalPhotoListEvent.ConfirmDelete);runCurrent()
        assertEquals(setOf("one"),vm.uiState.value.selected);assertEquals(1,effects.values.size)
        val gate=ControlledCall<Unit>();delete={gate.await()}
        vm.onEvent(LocalPhotoListEvent.ConfirmDelete);runCurrent()
        vm.onEvent(LocalPhotoListEvent.Select("two",true));vm.onEvent(LocalPhotoListEvent.ConfirmDelete);runCurrent()
        gate.complete(Unit);runCurrent();assertEquals(setOf("two"),vm.uiState.value.selected)
        assertEquals(listOf("child" to setOf("one"),"child" to setOf("one")),deleted)
        assertTrue(vm.uiState.value.pending.isEmpty());assertTrue(vm.uiState.value.editing)
    }
    @Test fun renameFailureKeepsDraftAndMissingParentClearsPrivateSelection()=runTest(main.testDispatcher){
        val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        vm.onEvent(LocalPhotoListEvent.Rename);vm.onEvent(LocalPhotoListEvent.Text("keep"));runCurrent()
        delete={throw IOException("rename")};vm.onEvent(LocalPhotoListEvent.SaveName);runCurrent()
        assertTrue(vm.uiState.value.rename);assertEquals("keep",vm.uiState.value.draft.text);assertEquals(1,effects.values.size)
        sub.value=null;runCurrent();assertEquals(ScreenState.Error(UiError.NotFound),vm.uiState.value.screen)
        assertNull(vm.uiState.value.header);assertFalse(vm.uiState.value.rename);assertEquals("",vm.uiState.value.draft.text)
    }
    @Test fun cancellationDoesNotEmitResultAndClearStopsLateSnapshots()=runTest(main.testDispatcher){
        val vm=vm();runCurrent();val effects=EffectRecorder(vm.effect,backgroundScope)
        delete={throw CancellationException()};vm.onEvent(LocalPhotoListEvent.Rename);vm.onEvent(LocalPhotoListEvent.SaveName);runCurrent()
        assertTrue(vm.uiState.value.pending.isEmpty());assertTrue(effects.values.isEmpty())
        store.clear();val before=vm.uiState.value;sub.value=null;runCurrent();assertEquals(before,vm.uiState.value)
    }
}
