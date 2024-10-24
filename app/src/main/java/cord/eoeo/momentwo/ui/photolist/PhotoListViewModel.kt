package cord.eoeo.momentwo.ui.photolist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.data.photo.PhotoRepository
import cord.eoeo.momentwo.domain.subalbum.ChangeSubAlbumTitleUseCase
import cord.eoeo.momentwo.ui.BaseViewModel
import cord.eoeo.momentwo.ui.MomentwoDestination
import cord.eoeo.momentwo.ui.photolist.PhotoListContract.Effect.ShowSnackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoListViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val changeSubAlbumTitleUseCase: ChangeSubAlbumTitleUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PhotoListContract.State, PhotoListContract.Event, PhotoListContract.Effect>() {
    init {
        val (albumId, subAlbumId, albumTitle, subAlbumTitle) = savedStateHandle.toRoute<MomentwoDestination.PhotoList>()

        viewModelScope.launch {
            setState(
                uiState.value.copy(
                    albumId = albumId,
                    subAlbumId = subAlbumId,
                    albumTitle = albumTitle,
                    subAlbumTitle = subAlbumTitle,
                    photoPagingData = photoRepository.getPhotoPagingData(50, albumId, subAlbumId),
                )
            )
        }
    }

    override fun createInitialState(): PhotoListContract.State = PhotoListContract.State()

    override fun handleEvent(newEvent: PhotoListContract.Event) {
        when (newEvent) {
            is PhotoListContract.Event.OnUploadImage -> {
                viewModelScope.launch {
                    with(uiState.value) {
                        Log.d("Photo", "OnClickAdd: ${newEvent.mimeType}, ${newEvent.imageUri}")
                        photoRepository.requestUploadPhoto(
                            albumId,
                            subAlbumId,
                            newEvent.mimeType,
                            newEvent.imageUri
                        )
                    }
                }
            }

            is PhotoListContract.Event.OnChangeIsRefreshing -> {
                with(uiState.value) { setState(copy(isRefreshing = newEvent.isRefreshing)) }
            }

            is PhotoListContract.Event.OnChangeIsMenuExpended -> {
                with(uiState.value) { setState(copy(isMenuExpended = newEvent.isMenuExpended)) }
            }

            is PhotoListContract.Event.OnChangeIsEditMode -> {
                with(uiState.value) { setState(copy(isEditMode = newEvent.isEditMode)) }
            }

            is PhotoListContract.Event.OnClickCancelEdit -> {
                with(uiState.value) {
                    setState(
                        copy(
                            isEditMode = false,
                            selectedPhotoIds = emptyList(),
                            selectedPhotoUrls = emptyList(),
                        )
                    )
                }
            }

            is PhotoListContract.Event.OnClickConfirmEdit -> {
                viewModelScope.launch {
                    with(uiState.value) {
                        photoRepository
                            .deletePhotos(albumId, subAlbumId, selectedPhotoIds, selectedPhotoUrls)
                            .onSuccess {
                                setState(
                                    copy(
                                        isEditMode = false,
                                        selectedPhotoIds = emptyList(),
                                        selectedPhotoUrls = emptyList(),
                                    )
                                )
                            }.onFailure {
                                /* TODO: 삭제 실패 */
                                Log.e("Photo", "deletePhotos Failure", it)
                            }
                    }
                }
            }

            is PhotoListContract.Event.OnChangeIsSelected -> {
                viewModelScope.launch {
                    with(uiState.value) {
                        val newSelectedIds = selectedPhotoIds.toMutableList()
                        val newSelectedUrls = selectedPhotoUrls.toMutableList()

                        if (newEvent.isSelected) {
                            newSelectedIds.add(newEvent.imageItem.id)
                            newSelectedUrls.add(newEvent.imageItem.imageUrl)
                        } else {
                            newSelectedIds.remove(newEvent.imageItem.id)
                            newSelectedUrls.remove(newEvent.imageItem.imageUrl)
                        }

                        setState(copy(selectedPhotoIds = newSelectedIds, selectedPhotoUrls = newSelectedUrls))
                    }
                }
            }

            is PhotoListContract.Event.OnChangeIsDialogOpened -> {
                with(uiState.value) { setState(copy(isDialogOpened = newEvent.isDialogOpened)) }
            }

            is PhotoListContract.Event.OnConfirmDialog -> {
                viewModelScope.launch {
                    with(uiState.value) {
                        changeSubAlbumTitleUseCase(albumId, subAlbumId, newEvent.subAlbumTitle)
                            .onSuccess {
                                setState(copy(subAlbumTitle = newEvent.subAlbumTitle, isDialogOpened = false))
                            }.onFailure {
                                /* TODO: 타이틀 변경 실패 */
                                Log.e("Photo", "changeSubAlbumTitle Failure", it)
                            }
                    }
                }
            }

            is PhotoListContract.Event.OnBack -> {
                setEffect { PhotoListContract.Effect.PopBackStack }
            }

            is PhotoListContract.Event.OnError -> {
                setEffect { ShowSnackbar(newEvent.errorMessage) }
            }
        }
    }
}
