package cord.eoeo.momentwo.feature.album.impl.album

import android.util.Log
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.domain.album.GetAlbumListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class AlbumViewModel @Inject constructor(private val getAlbumListUseCase: GetAlbumListUseCase) :
    ReducerViewModel<AlbumState, AlbumEvent, AlbumAction, AlbumEffect>(AlbumState(), AlbumReducer) {
    private data object ListRequest : RequestKey
    private var initialized = false
    fun initialize() {
        if ((!initialized || uiState.value.dirty) && !requests.isRunning(ListRequest)) {
            initialized = true
            load()
        }
    }
    override fun handleEvent(event: AlbumEvent) {
        when (event) {
            AlbumEvent.OnCloseDrawer -> sendEffect(AlbumEffect.CloseDrawer)
            AlbumEvent.OnGetAlbumList -> load()
            AlbumEvent.OnInvalidate -> { dispatch(AlbumAction.Invalidated); requests.cancel(ListRequest) }
            is AlbumEvent.OnError -> sendEffect(AlbumEffect.ShowSnackbar(event.errorMessage))
        }
    }
    private fun load() {
        val revision = uiState.value.revision
        requests.launchRequest(ListRequest, RequestPolicy.LatestWins,
            request = { getAlbumListUseCase().map { AlbumContent(it) } },
            onStarted = { dispatch(AlbumAction.Started) },
            onSuccess = { dispatch(AlbumAction.Loaded(it, revision)) },
            onFailure = { Log.e("Album", "Album list failed", it); dispatch(AlbumAction.Failed(it.toUiError())) },
            onFinished = { dispatch(AlbumAction.Finished) })
    }
}
