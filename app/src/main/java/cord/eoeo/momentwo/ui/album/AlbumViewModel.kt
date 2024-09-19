package cord.eoeo.momentwo.ui.album

import android.util.Log
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.domain.album.GetAlbumListUseCase
import cord.eoeo.momentwo.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel
    @Inject
    constructor(
        private val getAlbumListUseCase: GetAlbumListUseCase,
    ) : BaseViewModel<AlbumContract.State, AlbumContract.Event, AlbumContract.Effect>() {
        override fun createInitialState(): AlbumContract.State = AlbumContract.State()

        override fun handleEvent(newEvent: AlbumContract.Event) {
            when (newEvent) {
                is AlbumContract.Event.OnCloseDrawer -> {
                    setEffect { AlbumContract.Effect.CloseDrawer }
                }

                is AlbumContract.Event.OnGetAlbumList -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            setState(copy(isLoading = true))
                            getAlbumListUseCase()
                                .onSuccess { albumList ->
                                    setState(copy(albumList = albumList, isLoading = false, isSuccess = true))
                                }.onFailure { exception ->
                                    // TODO: 오류 표현
                                    setState(copy(isLoading = false, isError = true))
                                    Log.e("Album", "Get Album List Failure", exception)
                                }
                        }
                    }
                }

                is AlbumContract.Event.OnError -> {
                    setEffect { AlbumContract.Effect.ShowSnackbar(newEvent.errorMessage) }
                }
            }
        }
    }
