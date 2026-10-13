package cord.eoeo.momentwo.feature.photo.impl.photolist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.cachedIn
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.photo.PhotoRepository
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.domain.subalbum.ChangeSubAlbumTitleUseCase
import cord.eoeo.momentwo.feature.photo.api.PhotoListDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private fun initialState(handle: SavedStateHandle): PhotoListState {
    val route = handle.toRoute<PhotoListDestination>()
    return PhotoListState(route.albumId, route.subAlbumId, route.albumTitle, route.subAlbumTitle)
}
@HiltViewModel
internal class PhotoListViewModel(
    private val photoRepository: PhotoRepository,
    private val changeSubAlbumTitleUseCase: ChangeSubAlbumTitleUseCase,
    initialState: PhotoListState,
) : ReducerViewModel<PhotoListState, PhotoListEvent, PhotoListAction, PhotoListEffect>(initialState, PhotoListReducer) {
    @Inject constructor(photoRepository: PhotoRepository, changeSubAlbumTitleUseCase: ChangeSubAlbumTitleUseCase, handle: SavedStateHandle) :
        this(photoRepository, changeSubAlbumTitleUseCase, initialState(handle))
    private data object Source : RequestKey
    fun initialize() {
        if (uiState.value.source is ScreenState.Success) {
            if (uiState.value.dirty) { sendEffect(PhotoListEffect.RefreshPagingData); dispatch(PhotoListAction.DirtyChanged(false)) }
            return
        }
        val state = uiState.value
        requests.launchRequest(Source, RequestPolicy.IgnoreWhileRunning,
            request = { Result.success(photoRepository.getPhotoPagingData(20, state.albumId, state.subAlbumId).cachedIn(viewModelScope)) },
            onStarted = { dispatch(PhotoListAction.SourceStarted) },
            onSuccess = { dispatch(PhotoListAction.SourceReady(it)) },
            onFailure = { Log.e("PhotoList", "Paging source failed", it); dispatch(PhotoListAction.SourceFailed(it.toUiError())) })
    }
    override fun handleEvent(event: PhotoListEvent) {
        when (event) {
            is PhotoListEvent.OnChangeIsMenuExpended -> dispatch(PhotoListAction.MenuChanged(event.isMenuExpended))
            is PhotoListEvent.OnChangeIsEditMode -> dispatch(PhotoListAction.EditChanged(event.isEditMode))
            PhotoListEvent.OnClickCancelEdit -> dispatch(PhotoListAction.CancelEdit)
            is PhotoListEvent.OnChangeIsSelected -> dispatch(PhotoListAction.SelectionChanged(event.isSelected, event.photoId))
            is PhotoListEvent.OnChangeIsDialogOpened -> dispatch(PhotoListAction.DialogChanged(event.isDialogOpened))
            PhotoListEvent.OnRenameDraftChanged -> dispatch(PhotoListAction.DialogEdited)
            PhotoListEvent.OnPhotoVisited -> dispatch(PhotoListAction.DirtyChanged(true))
            PhotoListEvent.OnClickConfirmEdit -> delete()
            is PhotoListEvent.OnConfirmDialog -> rename(event.subAlbumTitle)
            is PhotoListEvent.OnUploadImage -> {
                val state = uiState.value
                write(PhotoListWriteKey.Upload(event.imageUri.toString()), "사진을 추가하지 못했습니다",
                    request = { photoRepository.requestUploadPhoto(state.albumId, state.subAlbumId, event.imageUri) },
                    success = { emitEffect(PhotoListEffect.RefreshPagingData) })
            }
            PhotoListEvent.OnRefreshPagingData -> sendEffect(PhotoListEffect.RefreshPagingData)
            PhotoListEvent.OnBack -> sendEffect(PhotoListEffect.PopBackStack)
            is PhotoListEvent.OnError -> sendEffect(PhotoListEffect.ShowSnackbar(event.errorMessage))
        }
    }
    private fun delete() {
        val ids = uiState.value.selectedPhotoIds.toList()
        if (ids.isEmpty()) return
        val session = uiState.value.editSession
        val albumId = uiState.value.albumId
        val subAlbumId = uiState.value.subAlbumId
        write(PhotoListWriteKey.Delete, "사진을 삭제하지 못했습니다",
            request = { photoRepository.deletePhotos(albumId, subAlbumId, ids) },
            success = { dispatch(PhotoListAction.Deleted(ids, session)); emitEffect(PhotoListEffect.RefreshPagingData) },
            committedFailure = { dispatch(PhotoListAction.Deleted(ids, session)); emitEffect(PhotoListEffect.RefreshPagingData) })
    }
    private fun rename(title: String) {
        val revision = uiState.value.dialogRevision
        val albumId = uiState.value.albumId
        val subAlbumId = uiState.value.subAlbumId
        write(PhotoListWriteKey.Rename, "제목을 변경하지 못했습니다", request = { changeSubAlbumTitleUseCase(albumId, subAlbumId, title) },
            success = { dispatch(PhotoListAction.Renamed(title, revision)) })
    }
    private fun write(key: PhotoListWriteKey, message: String, request: suspend () -> Result<Unit>, success: suspend () -> Unit, committedFailure: (suspend () -> Unit)? = null) {
        requests.launchRequest(key, RequestPolicy.IgnoreWhileRunning, request,
            onStarted = { dispatch(PhotoListAction.PendingChanged(key, true)) }, onSuccess = { success() },
            onFailure = {
                Log.e("PhotoList", "Operation failed", it)
                if (it is cord.eoeo.momentwo.core.data.CommittedWriteException && committedFailure != null) {
                    committedFailure(); emitEffect(PhotoListEffect.ShowSnackbar("사진은 삭제되었습니다. 목록을 새로고침해주세요"))
                } else emitEffect(PhotoListEffect.ShowSnackbar(message))
            }, onFinished = { dispatch(PhotoListAction.PendingChanged(key, false)) })
    }
}
