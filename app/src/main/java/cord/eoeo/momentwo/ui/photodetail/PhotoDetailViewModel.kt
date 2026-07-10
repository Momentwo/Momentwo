package cord.eoeo.momentwo.ui.photodetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cord.eoeo.momentwo.core.data.comment.CommentRepository
import cord.eoeo.momentwo.core.data.description.DescriptionRepository
import cord.eoeo.momentwo.core.data.like.LikeRepository
import cord.eoeo.momentwo.domain.photo.DownloadPhotoUseCase
import cord.eoeo.momentwo.domain.photo.UpdateIsLikedUseCase
import cord.eoeo.momentwo.core.common.BaseViewModel
import cord.eoeo.momentwo.ui.MomentwoDestination
import cord.eoeo.momentwo.core.model.DescriptionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoDetailViewModel
    @Inject
    constructor(
        private val descriptionRepository: DescriptionRepository,
        private val likeRepository: LikeRepository,
        private val commentRepository: CommentRepository,
        private val downloadPhotoUseCase: DownloadPhotoUseCase,
        private val updateIsLikedUseCase: UpdateIsLikedUseCase,
        savedStateHandle: SavedStateHandle,
    ) : BaseViewModel<PhotoDetailContract.State, PhotoDetailContract.Event, PhotoDetailContract.Effect>() {
        init {
            val (albumId, photoId, photoUrl, isLiked) = savedStateHandle.toRoute<MomentwoDestination.PhotoDetail>()

            viewModelScope.launch {
                setState(
                    uiState.value.copy(
                        albumId = albumId,
                        photoId = photoId,
                        photoUrl = photoUrl,
                        isLiked = isLiked,
                        commentPagingData = commentRepository.getCommentPagingData(albumId, photoId, 10),
                    ),
                )
            }
        }

        override fun createInitialState(): PhotoDetailContract.State = PhotoDetailContract.State()

        override fun handleEvent(newEvent: PhotoDetailContract.Event) {
            when (newEvent) {
                is PhotoDetailContract.Event.OnToggleIsMenuVisible -> {
                    with(uiState.value) {
                        setState(copy(isMenuVisible = isMenuVisible.not()))
                        setEffect { PhotoDetailContract.Effect.ToggleSystemBars(isMenuVisible.not()) }
                    }
                }

                is PhotoDetailContract.Event.OnToggleIsLiked -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            val currentTime = System.currentTimeMillis()

                            if (currentTime - likeLastUpdated > 1000L) {
                                if (isLiked.not()) {
                                    setState(copy(isLiked = true, likeLastUpdated = currentTime, likeCount = likeCount + 1))
                                    likeRepository.requestDoLike(albumId, photoId)
                                } else {
                                    setState(
                                        copy(
                                            isLiked = false,
                                            likeLastUpdated = currentTime,
                                            likeCount = likeCount - 1,
                                        ),
                                    )
                                    likeRepository.requestUndoLike(albumId, photoId)
                                }.onSuccess {
                                    updateIsLikedUseCase(photoId, isLiked.not())
                                }.onFailure { exception ->
                                    // TODO
                                    Log.e("Desc", "OnToggleIsLiked Failure", exception)
                                }
                            }
                        }
                    }
                }

                is PhotoDetailContract.Event.OnGetLikeCount -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            likeRepository
                                .getLikeCount(albumId, photoId)
                                .onSuccess { count ->
                                    setState(copy(likeCount = count))
                                }.onFailure { exception ->
                                    // TODO
                                    Log.e("Desc", "getLikeCount Failure", exception)
                                }
                        }
                    }
                }

                is PhotoDetailContract.Event.OnChangeIsDescriptionVisible -> {
                    with(uiState.value) { setState(copy(isDescriptionVisible = newEvent.isDescriptionVisible)) }
                }

                is PhotoDetailContract.Event.OnChangeIsWriteDescMode -> {
                    with(uiState.value) { setState(copy(isWriteDescMode = newEvent.isWriteDescMode)) }
                }

                is PhotoDetailContract.Event.OnGetDescription -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            descriptionRepository
                                .getDescription(albumId, photoId)
                                .onSuccess { desc ->
                                    setState(copy(descriptionItem = desc))
                                }.onFailure { exception ->
                                    // TODO
                                    Log.e("Desc", "getDescription Failure", exception)
                                }
                        }
                    }
                }

                is PhotoDetailContract.Event.OnWriteDescription -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            val descString = StringBuilder().appendLine(newEvent.title).append(newEvent.contents).toString()

                            descriptionRepository
                                .writeDescription(albumId, photoId, descString)
                                .onSuccess {
                                    setEvent(PhotoDetailContract.Event.OnGetDescription)
                                    setEvent(PhotoDetailContract.Event.OnChangeIsWriteDescMode(false))
                                }.onFailure { exception ->
                                    // TODO
                                    Log.e("Desc", "writeDescription Failure", exception)
                                }
                        }
                    }
                }

                is PhotoDetailContract.Event.OnEditDescription -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            val descString = StringBuilder().appendLine(newEvent.title).append(newEvent.contents).toString()

                            descriptionRepository
                                .editDescription(albumId, photoId, descString)
                                .onSuccess {
                                    setEvent(PhotoDetailContract.Event.OnGetDescription)
                                }.onFailure { exception ->
                                    // TODO
                                    Log.e("Desc", "editDescription Failure", exception)
                                }
                        }
                    }
                }

                is PhotoDetailContract.Event.OnDeleteDescription -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            descriptionRepository
                                .deleteDescription(albumId, photoId)
                                .onSuccess {
                                    setState(copy(descriptionItem = DescriptionItem("", "", "", "", emptyList())))
                                }.onFailure { exception ->
                                    // TODO
                                    Log.e("Desc", "deleteDescription Failure", exception)
                                }
                        }
                    }
                }

                is PhotoDetailContract.Event.OnChangeIsCommentVisible -> {
                    with(uiState.value) { setState(copy(isCommentVisible = newEvent.isCommentVisible)) }
                }

                is PhotoDetailContract.Event.OnScrollCommentToTop -> {
                    setEffect { PhotoDetailContract.Effect.ScrollCommentToTop }
                }

                is PhotoDetailContract.Event.OnWriteComment -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            commentRepository
                                .writeComment(albumId, photoId, newEvent.comment)
                                .onSuccess {
                                    setEffect { PhotoDetailContract.Effect.RefreshCommentPagingData }
                                    setEffect { PhotoDetailContract.Effect.ScrollCommentToBottom }
                                }.onFailure { exception ->
                                    // TODO
                                    Log.e("Comment", "writeComment Failure", exception)
                                }
                        }
                    }
                }

                is PhotoDetailContract.Event.OnEditComment -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            commentRepository
                                .editComment(albumId, newEvent.commentId, newEvent.comment)
                                .onSuccess {
                                    setEffect { PhotoDetailContract.Effect.RefreshCommentPagingData }
                                }.onFailure { exception ->
                                    // TODO
                                    Log.e("Comment", "editComment Failure", exception)
                                }
                        }
                    }
                }

                is PhotoDetailContract.Event.OnDeleteComment -> {
                    viewModelScope.launch {
                        with(uiState.value) {
                            commentRepository
                                .deleteComment(albumId, newEvent.commentId)
                                .onSuccess {
                                    setEffect { PhotoDetailContract.Effect.RefreshCommentPagingData }
                                }.onFailure { exception ->
                                    // TODO
                                    Log.e("Comment", "writeComment Failure", exception)
                                }
                        }
                    }
                }

                is PhotoDetailContract.Event.OnDownloadPhoto -> {
                    viewModelScope.launch {
                        downloadPhotoUseCase(uiState.value.photoUrl)
                            .onSuccess {
                                // TODO
                                setEvent(PhotoDetailContract.Event.OnShowSnackbar("사진 다운로드를 완료했습니다"))
                                Log.d("Photo", "downloadPhoto Success")
                            }.onFailure { exception ->
                                // TODO
                                setEvent(PhotoDetailContract.Event.OnShowSnackbar("사진 다운로드에 실패했습니다"))
                                Log.e("Photo", "downloadPhoto Failure", exception)
                            }
                    }
                }

                is PhotoDetailContract.Event.OnBack -> {
                    setEffect { PhotoDetailContract.Effect.PopBackStack }
                }

                is PhotoDetailContract.Event.OnShowSnackbar -> {
                    setEffect { PhotoDetailContract.Effect.ShowSnackbar(newEvent.message) }
                }
            }
        }
    }
