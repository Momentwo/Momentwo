package cord.eoeo.momentwo.feature.photo.impl
import android.net.Uri
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import cord.eoeo.momentwo.core.data.library.LocalLibraryRepository
import cord.eoeo.momentwo.core.domain.subalbum.ChangeSubAlbumTitleUseCase
import cord.eoeo.momentwo.core.model.LocalSubAlbum
import cord.eoeo.momentwo.core.testing.*
import cord.eoeo.momentwo.feature.photo.impl.local.*
import cord.eoeo.momentwo.feature.photo.impl.photolist.*
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PhotoUriViewModelTest {
    @get:Rule val main=MainDispatcherRule(StandardTestDispatcher())
    private val store=ViewModelStore()
    @After fun clear()=store.clear()
    @Test fun uploadSnapshotsRealUriSkipsDuplicateAndRefreshesOnlyOnSuccess()=runTest(main.testDispatcher){
        val repo=PhotoFakeRepository();val calls=mutableListOf<Uri>();val gate=ControlledCall<Unit>()
        repo.upload={calls+=it;gate.await();Result.success(Unit)}
        val vm=PhotoListViewModel(repo,ChangeSubAlbumTitleUseCase(PhotoFakeSubAlbum()),PhotoListState(1,2,"a","s")).also{store.put("server",it)}
        val effects=EffectRecorder(vm.effect,backgroundScope);val uri=Uri.parse("content://fixture/one")
        vm.onEvent(PhotoListEvent.OnUploadImage(uri));runCurrent();vm.onEvent(PhotoListEvent.OnUploadImage(uri));runCurrent()
        assertEquals(listOf(uri),calls);gate.complete(Unit);runCurrent()
        assertEquals(listOf(PhotoListEffect.RefreshPagingData),effects.values);assertTrue(vm.uiState.value.pendingWrites.isEmpty())
        repo.upload={Result.failure(IOException("upload"))};vm.onEvent(PhotoListEvent.OnUploadImage(uri));runCurrent()
        assertTrue(effects.values.last() is PhotoListEffect.ShowSnackbar)
        assertEquals(1,effects.values.count{it==PhotoListEffect.RefreshPagingData})
        repo.upload={Result.failure(CancellationException())};vm.onEvent(PhotoListEvent.OnUploadImage(uri));runCurrent()
        assertEquals(2,effects.values.size);assertTrue(vm.uiState.value.pendingWrites.isEmpty())
    }
    @Test fun importPartialFailureUsesOriginalUrisAndCancellationPublishesNoCompletion()=runTest(main.testDispatcher){
        val core=PhotoLocalFake();val calls=mutableListOf<Uri>();val ids=mutableListOf<String>()
        var cancelled=false
        val repo=object:LocalLibraryRepository by core{
            override fun subAlbum(id:String)=flowOf(LocalSubAlbum("child","album","s",null,0))
            override suspend fun importPhoto(subAlbumId:String,source:Uri,localId:String):String {
                assertEquals("child",subAlbumId);calls+=source;ids+=localId
                if(cancelled)throw CancellationException()
                if(source.lastPathSegment=="bad")throw IOException("read")
                return localId
            }
        }
        val vm=LocalPhotoListViewModel(repo,"child").also{store.put("local",it);it.initialize()};runCurrent()
        val effects=EffectRecorder(vm.effect,backgroundScope)
        val uris=listOf(Uri.parse("content://fixture/good"),Uri.parse("content://fixture/bad"))
        vm.onEvent(LocalPhotoListEvent.Import(uris));runCurrent()
        assertEquals(uris,calls);assertEquals(2,ids.toSet().size)
        assertEquals(1,effects.values.size);assertTrue((effects.values.single() as LocalPhotoListEffect.Message).text.contains("1장"))
        assertFalse(vm.uiState.value.importing)
        cancelled=true;vm.onEvent(LocalPhotoListEvent.Import(uris));runCurrent()
        assertEquals(1,effects.values.size);assertFalse(vm.uiState.value.importing)
    }
}
