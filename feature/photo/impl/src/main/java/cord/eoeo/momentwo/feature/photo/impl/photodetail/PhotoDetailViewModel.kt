package cord.eoeo.momentwo.feature.photo.impl.photodetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.cachedIn
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.comment.CommentRepository
import cord.eoeo.momentwo.core.data.description.DescriptionRepository
import cord.eoeo.momentwo.core.data.like.LikeRepository
import cord.eoeo.momentwo.core.data.toUiError
import cord.eoeo.momentwo.core.domain.photo.DownloadPhotoUseCase
import cord.eoeo.momentwo.core.domain.photo.UpdateIsLikedUseCase
import cord.eoeo.momentwo.feature.photo.api.PhotoDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

private fun initialState(handle: SavedStateHandle): PhotoDetailState {
    val route = handle.toRoute<PhotoDetailDestination>()
    return PhotoDetailState(route.albumId, route.photoId, route.photoUrl, route.isLiked)
}
@HiltViewModel
internal class PhotoDetailViewModel(
    private val descriptionRepository: DescriptionRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository,
    private val downloadPhotoUseCase: DownloadPhotoUseCase,
    private val updateIsLikedUseCase: UpdateIsLikedUseCase,
    initialState: PhotoDetailState,
    private val clock: () -> Long = System::currentTimeMillis,
) : ReducerViewModel<PhotoDetailState, PhotoDetailEvent, PhotoDetailAction, PhotoDetailEffect>(initialState, PhotoDetailReducer) {
    @Inject constructor(descriptionRepository: DescriptionRepository, likeRepository: LikeRepository, commentRepository: CommentRepository,
        downloadPhotoUseCase: DownloadPhotoUseCase, updateIsLikedUseCase: UpdateIsLikedUseCase, handle: SavedStateHandle) :
        this(descriptionRepository, likeRepository, commentRepository, downloadPhotoUseCase, updateIsLikedUseCase, initialState(handle))
    private data object Read : RequestKey
    private data object Comments : RequestKey
    private var initialized = false
    private var refreshRequested = false
    private val albumId get() = uiState.value.albumId
    private val photoId get() = uiState.value.photoId

    fun initialize() { if (!initialized) { initialized = true; reload() } }
    override fun handleEvent(event: PhotoDetailEvent) {
        when (event) {
            is PhotoDetailEvent.OnOpenCommentDialog -> dispatch(PhotoCommentAction.DialogOpened(event.id, event.text))
            PhotoDetailEvent.OnDismissCommentDialog -> dispatch(PhotoCommentAction.DialogDismissed)
            PhotoDetailEvent.OnCommentEditDraftChanged -> dispatch(PhotoCommentAction.DialogEdited)
            PhotoDetailEvent.OnRetry, PhotoDetailEvent.OnGetLikeCount, PhotoDetailEvent.OnGetDescription -> reload()
            PhotoDetailEvent.OnToggleIsMenuVisible -> dispatch(PhotoUiAction.ToggleMenu)
            PhotoDetailEvent.OnToggleIsLiked -> toggleLike()
            is PhotoDetailEvent.OnChangeIsDescriptionVisible -> { dispatch(PhotoUiAction.DescriptionVisible(event.isDescriptionVisible)); if (event.isDescriptionVisible) reload() }
            is PhotoDetailEvent.OnChangeIsWriteDescMode -> dispatch(PhotoDescriptionAction.Editing(event.isWriteDescMode))
            PhotoDetailEvent.OnDescriptionDraftChanged -> dispatch(PhotoDescriptionAction.DraftEdited)
            is PhotoDetailEvent.OnWriteDescription -> saveDescription(event.title, event.contents, edit = false)
            is PhotoDetailEvent.OnEditDescription -> saveDescription(event.title, event.contents, edit = true)
            PhotoDetailEvent.OnDeleteDescription -> {
                val revision = uiState.value.descriptionRevision
                write(PhotoWriteKey.Description, request = { descriptionRepository.deleteDescription(albumId, photoId) },
                    before = { requests.cancel(Read) }, success = { dispatch(PhotoDescriptionAction.Deleted(revision)) })
            }
            is PhotoDetailEvent.OnChangeIsCommentVisible -> { dispatch(PhotoUiAction.CommentVisible(event.isCommentVisible)); if (event.isCommentVisible) loadComments() }
            is PhotoDetailEvent.OnCommentDraftChanged -> dispatch(PhotoCommentAction.DraftChanged(event.text))
            PhotoDetailEvent.OnScrollCommentToTop -> sendEffect(PhotoDetailEffect.ScrollCommentToTop)
            is PhotoDetailEvent.OnWriteComment -> {
                val revision = uiState.value.commentRevision
                write(PhotoWriteKey.NewComment, request = { commentRepository.writeComment(albumId, photoId, event.comment) }, success = {
                    dispatch(PhotoCommentAction.Sent(revision))
                    emitEffect(PhotoDetailEffect.RefreshCommentPagingData)
                    emitEffect(PhotoDetailEffect.ScrollCommentToBottom)
                })
            }
            is PhotoDetailEvent.OnEditComment -> {
                val revision = uiState.value.commentDialogRevision
                write(PhotoWriteKey.EditComment(event.commentId), request = { commentRepository.editComment(albumId, event.commentId, event.comment) }, success = {
                    dispatch(PhotoCommentAction.Edited(event.commentId, revision)); emitEffect(PhotoDetailEffect.RefreshCommentPagingData)
                })
            }
            is PhotoDetailEvent.OnDeleteComment -> write(PhotoWriteKey.DeleteComment(event.commentId),
                request = { commentRepository.deleteComment(albumId, event.commentId) }, success = { emitEffect(PhotoDetailEffect.RefreshCommentPagingData) })
            PhotoDetailEvent.OnDownloadPhoto -> write(PhotoWriteKey.Download,
                request = { downloadPhotoUseCase(uiState.value.photoUrl) }, success = { emitEffect(PhotoDetailEffect.ShowSnackbar("사진 다운로드를 완료했습니다")) })
            PhotoDetailEvent.OnBack -> sendEffect(PhotoDetailEffect.PopBackStack)
            is PhotoDetailEvent.OnShowSnackbar -> sendEffect(PhotoDetailEffect.ShowSnackbar(event.message))
        }
    }
    private fun reload() {
        if (requests.isRunning(PhotoWriteKey.Like) || requests.isRunning(PhotoWriteKey.Description)) { refreshRequested = true; return }
        refreshRequested = false
        val likeVersion = uiState.value.likeVersion
        val descriptionVersion = uiState.value.descriptionVersion
        requests.launchRequest(Read, RequestPolicy.LatestWins,
            request = { runSuspendCatching { coroutineScope {
                val description = async { descriptionRepository.getDescription(albumId, photoId).getOrThrow() }
                val count = async { likeRepository.getLikeCount(albumId, photoId).getOrThrow() }
                description.await() to count.await()
            } } },
            onStarted = { dispatch(PhotoDetailAction.Started) },
            onSuccess = { dispatch(PhotoDetailAction.Loaded(it.first, it.second, likeVersion, descriptionVersion)) },
            onFailure = { Log.e("PhotoDetail", "Initial/refresh load failed", it); dispatch(PhotoDetailAction.Failed(it.toUiError())) },
            onFinished = { dispatch(PhotoDetailAction.Finished) })
    }
    private fun loadComments() {
        if (uiState.value.commentsReady) return
        requests.launchRequest(Comments, RequestPolicy.IgnoreWhileRunning,
            request = { Result.success(commentRepository.getCommentPagingData(albumId, photoId, 10).cachedIn(viewModelScope)) },
            onSuccess = { dispatch(PhotoCommentAction.SourceReady(it)) },
            onFailure = { Log.e("PhotoDetail", "Comment source failed", it); dispatch(PhotoCommentAction.SourceFailed(it.toUiError())) })
    }
    private fun toggleLike() {
        val content = (uiState.value.screen as? ScreenState.Success)?.content ?: return
        if (requests.isRunning(PhotoWriteKey.Like)) return
        val now = clock()
        if (uiState.value.likeLastUpdated?.let { now - it <= 1000L } == true) return
        val liked = !content.isLiked
        val version = uiState.value.likeVersion + 1
        requests.launchRequest(PhotoWriteKey.Like, RequestPolicy.IgnoreWhileRunning,
            request = { if (liked) likeRepository.requestDoLike(albumId, photoId) else likeRepository.requestUndoLike(albumId, photoId) },
            onStarted = {
                requests.cancel(Read)
                dispatch(PhotoDetailAction.PendingChanged(PhotoWriteKey.Like, true))
                dispatch(PhotoLikeAction.Optimistic(liked, (content.likeCount + if (liked) 1 else -1).coerceAtLeast(0), now))
            },
            onSuccess = {
                // Remote ACK and local cache update are distinct outcomes; cancellation still propagates.
                runSuspendCatching { updateIsLikedUseCase(photoId, liked) }.onFailure {
                    Log.e("PhotoDetail", "Like committed; local update failed", it)
                    emitEffect(PhotoDetailEffect.ShowSnackbar("좋아요는 반영되었습니다. 목록을 새로고침해주세요"))
                }
            },
            onFailure = { Log.e("PhotoDetail", "Like failed", it); dispatch(PhotoLikeAction.Rollback(content.isLiked, content.likeCount, version)); emitEffect(PhotoDetailEffect.ShowSnackbar("좋아요를 변경하지 못했습니다")) },
            onFinished = { dispatch(PhotoDetailAction.PendingChanged(PhotoWriteKey.Like, false)); refreshAfterWrites() })
    }
    private fun saveDescription(title: String, contents: String, edit: Boolean) {
        val text = "$title\n$contents"
        val revision = uiState.value.descriptionRevision
        write(PhotoWriteKey.Description, request = { if (edit) descriptionRepository.editDescription(albumId, photoId, text) else descriptionRepository.writeDescription(albumId, photoId, text) },
            before = { requests.cancel(Read) }, success = { dispatch(PhotoDescriptionAction.Committed(revision)); refreshRequested = true })
    }
    private fun write(key: PhotoWriteKey, request: suspend () -> Result<Unit>, before: () -> Unit = {}, success: suspend () -> Unit) {
        requests.launchRequest(key, RequestPolicy.IgnoreWhileRunning, request,
            onStarted = { before(); dispatch(PhotoDetailAction.PendingChanged(key, true)) }, onSuccess = { success() },
            onFailure = { Log.e("PhotoDetail", "Operation failed", it); emitEffect(PhotoDetailEffect.ShowSnackbar("작업을 완료하지 못했습니다")) },
            onFinished = { dispatch(PhotoDetailAction.PendingChanged(key, false)); refreshAfterWrites() })
    }
    private fun refreshAfterWrites() {
        if (refreshRequested && !requests.isRunning(PhotoWriteKey.Like) && !requests.isRunning(PhotoWriteKey.Description)) reload()
    }
}
