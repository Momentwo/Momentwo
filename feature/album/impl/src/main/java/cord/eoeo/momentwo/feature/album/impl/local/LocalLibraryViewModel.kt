package cord.eoeo.momentwo.feature.album.impl.local

import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.LocalLibraryRepository
import cord.eoeo.momentwo.core.model.LibraryScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch

@HiltViewModel
internal class LocalLibraryViewModel @Inject constructor(private val repository: LocalLibraryRepository) :
    ReducerViewModel<LocalLibraryState,LocalLibraryEvent,LocalLibraryAction,LocalLibraryEffect>(LocalLibraryState(),LocalLibraryReducer) {
    private var observation: Job? = null
    fun initialize() {
        if(observation?.isActive==true)return
        observation=viewModelScope.launch {
            runSuspendCatching { repository.initialize() }.onFailure { Log.e("LocalLibrary","Initialization failed",it);dispatch(LocalLibraryAction.Failed);return@launch }
            runSuspendCatching { repository.cleanUnusedMedia() }.onFailure { Log.e("LocalLibrary","Cleanup deferred",it) }
            repository.albums(LibraryScope.Device).catch {
                if(it is CancellationException || it is Error)throw it
                Log.e("LocalLibrary","Observation failed",it);dispatch(LocalLibraryAction.Failed)
            }.collect { dispatch(LocalLibraryAction.Loaded(it)) }
        }
    }
    override fun handleEvent(event: LocalLibraryEvent) { when(event) {
        LocalLibraryEvent.Retry -> initialize()
        LocalLibraryEvent.ConnectAccount -> sendEffect(LocalLibraryEffect.Login)
        is LocalLibraryEvent.OpenAlbum -> sendEffect(LocalLibraryEffect.Album(event.id))
    } }
}
