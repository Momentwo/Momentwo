package cord.eoeo.momentwo.feature.photo.impl.local
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.LocalLibraryRepository
import cord.eoeo.momentwo.feature.photo.api.LocalPhotoListDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID
@HiltViewModel
internal class LocalPhotoListViewModel(private val repository: LocalLibraryRepository, id: String) : ReducerViewModel<LocalPhotoListState, LocalPhotoListEvent, LocalPhotoListAction, LocalPhotoListEffect>(LocalPhotoListState(id), LocalPhotoListReducer) {
    @Inject constructor(repository: LocalLibraryRepository, handle: SavedStateHandle) : this(repository, handle.toRoute<LocalPhotoListDestination>().subAlbumId)
    private data object Import : RequestKey
    private var observation: Job? = null
    fun initialize() {
        if(observation?.isActive == true) return
        observation = viewModelScope.launch {
            combine(repository.subAlbum(uiState.value.id), repository.photos(uiState.value.id)) { sub, photos -> sub?.let { LocalPhotoListContent(it,photos) } }
            .catch { if(it is CancellationException || it is Error) throw it; Log.e("LocalPhotos", "Observation failed", it); dispatch(LocalPhotoListAction.Failed(UiError.Unknown)) }
            .collect { if(it == null) dispatch(LocalPhotoListAction.Failed(UiError.NotFound)) else dispatch(LocalPhotoListAction.Snapshot(it)) }
        }
    }
    override fun handleEvent(event: LocalPhotoListEvent) {
        when(event) {
            LocalPhotoListEvent.Retry -> initialize()
            LocalPhotoListEvent.Back -> sendEffect(LocalPhotoListEffect.Back)
            is LocalPhotoListEvent.Open -> sendEffect(LocalPhotoListEffect.Open(event.id))
            is LocalPhotoListEvent.Import -> {
                if(event.sources.isEmpty() || uiState.value.screen !is ScreenState.Success) return
                val id = uiState.value.id
                val sources = event.sources.map { it to UUID.randomUUID().toString() }
                requests.launchRequest(Import, RequestPolicy.IgnoreWhileRunning, { runSuspendCatching {
                    var saved = 0
                    sources.forEach { (uri, intentId) -> runSuspendCatching { repository.importPhoto(id,uri,intentId) }.onSuccess { saved++ }.onFailure { Log.e("LocalPhotos", "Import failed", it) } }
                    saved
                } }, onStarted = { dispatch(LocalPhotoListAction.Importing(true)) }, onSuccess = { emitEffect(LocalPhotoListEffect.Message("${it}장의 사진을 기기에 저장했습니다" + if(it < sources.size) "; 나머지 사진은 가져오지 못했습니다" else "")) }, onFailure = { Log.e("LocalPhotos", "Import failed", it); emitEffect(LocalPhotoListEffect.Message("사진을 보관하지 못했습니다")) }, onFinished = { dispatch(LocalPhotoListAction.Importing(false)) })
            }
        }
    }
}
