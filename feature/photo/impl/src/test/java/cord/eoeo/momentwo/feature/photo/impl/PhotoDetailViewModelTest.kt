package cord.eoeo.momentwo.feature.photo.impl

import androidx.lifecycle.ViewModelStore
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.domain.photo.*
import cord.eoeo.momentwo.core.model.DescriptionItem
import cord.eoeo.momentwo.core.testing.MainDispatcherRule
import cord.eoeo.momentwo.feature.photo.impl.photodetail.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoDetailViewModelTest {
    @get:Rule val main = MainDispatcherRule(StandardTestDispatcher())
    private val photo = PhotoFakeRepository()
    private val description = PhotoFakeDescription()
    private val likes = PhotoFakeLike()
    private val comments = PhotoFakeComment()
    private val store = ViewModelStore()
    private var now = 0L
    private fun vm() = PhotoDetailViewModel(description, likes, comments, DownloadPhotoUseCase(photo), UpdateIsLikedUseCase(photo),
        PhotoDetailState(1, 2, "fixture", false), { now }).also { store.put("detail", it) }
    @After fun clear() { store.clear() }
    private fun content(vm: PhotoDetailViewModel) = (vm.uiState.value.screen as ScreenState.Success).content

    @Test fun initialSuccessWaitsForBothResponsesAndCommentsAreLazy() = runTest(main.testDispatcher) {
        val response = CompletableDeferred<DescriptionItem>()
        description.read = { Result.success(response.await()) }
        val vm = vm(); vm.initialize(); runCurrent()
        assertEquals(ScreenState.Loading, vm.uiState.value.screen)
        assertEquals(0, comments.sourceCalls)
        response.complete(fixtureDescription); advanceUntilIdle()
        assertEquals(10, content(vm).likeCount)
        assertEquals(fixtureDescription, content(vm).description)
        vm.onEvent(PhotoDetailEvent.OnChangeIsCommentVisible(true)); advanceUntilIdle()
        assertEquals(1, comments.sourceCalls)
    }
    @Test fun partialFailureDoesNotExposeFakeCountOrDescription() = runTest(main.testDispatcher) {
        description.read = { Result.failure(IOException("offline")) }
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        assertEquals(ScreenState.Error(UiError.Network), vm.uiState.value.screen)
        assertTrue(vm.uiState.value.barsVisible)
    }
    @Test fun likeFailureRollsBackOnlyLikeFieldsAndPreservesMenuState() = runTest(main.testDispatcher) {
        val response = CompletableDeferred<Unit>()
        likes.write = { response.await(); Result.failure(IOException("offline")) }
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        vm.onEvent(PhotoDetailEvent.OnToggleIsLiked); runCurrent()
        assertEquals(11, content(vm).likeCount)
        vm.onEvent(PhotoDetailEvent.OnToggleIsMenuVisible)
        vm.onEvent(PhotoDetailEvent.OnToggleIsLiked)
        response.complete(Unit); advanceUntilIdle()
        assertEquals(1, likes.calls)
        assertFalse(content(vm).isLiked)
        assertEquals(10, content(vm).likeCount)
        assertFalse(vm.uiState.value.isMenuVisible)
        assertEquals(fixtureDescription, content(vm).description)
    }
    @Test fun committedLikeIsNotRolledBackOnLocalUpdateFailure() = runTest(main.testDispatcher) {
        photo.update = { throw IOException("local storage failed") }
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        vm.onEvent(PhotoDetailEvent.OnToggleIsLiked); advanceUntilIdle()
        assertTrue(content(vm).isLiked)
        assertEquals(11, content(vm).likeCount)
        assertTrue(vm.effect.first() is PhotoDetailEffect.ShowSnackbar)
    }
    @Test fun oldCountResponseCannotOverwriteOptimisticLike() = runTest(main.testDispatcher) {
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        val old = CompletableDeferred<Int>()
        likes.read = { Result.success(old.await()) }
        vm.onEvent(PhotoDetailEvent.OnGetLikeCount); runCurrent()
        vm.onEvent(PhotoDetailEvent.OnToggleIsLiked); runCurrent()
        old.complete(1); advanceUntilIdle()
        assertEquals(11, content(vm).likeCount)
        assertTrue(content(vm).isLiked)
    }
    @Test fun likeThrottleUsesInjectedTimeAndKeepsOneSecondThreshold() = runTest(main.testDispatcher) {
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        vm.onEvent(PhotoDetailEvent.OnToggleIsLiked); advanceUntilIdle()
        now = 1000; vm.onEvent(PhotoDetailEvent.OnToggleIsLiked); advanceUntilIdle()
        assertEquals(1, likes.calls)
        now = 1001; vm.onEvent(PhotoDetailEvent.OnToggleIsLiked); advanceUntilIdle()
        assertEquals(2, likes.calls)
    }
    @Test fun descriptionCommitAndRefreshFailureKeepEditorAndNewDraftContext() = runTest(main.testDispatcher) {
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        vm.onEvent(PhotoDetailEvent.OnChangeIsWriteDescMode(true))
        description.read = { Result.failure(IOException("refresh failed")) }
        vm.onEvent(PhotoDetailEvent.OnWriteDescription("new title", "new contents")); advanceUntilIdle()
        assertEquals(listOf("new title\nnew contents"), description.writes)
        assertTrue(vm.uiState.value.isWriteDescMode)
        assertNotNull(vm.uiState.value.confirmedDescriptionRevision)
        vm.onEvent(PhotoDetailEvent.OnDescriptionDraftChanged)
        description.read = { Result.success(description.description) }
        vm.onEvent(PhotoDetailEvent.OnRetry); advanceUntilIdle()
        assertTrue(vm.uiState.value.isWriteDescMode)
        assertNull(vm.uiState.value.confirmedDescriptionRevision)
        assertEquals(description.description, content(vm).description)
    }
    @Test fun commentSuccessKeepsNewDraftAndEffectsAreOrdered() = runTest(main.testDispatcher) {
        val response = CompletableDeferred<Unit>()
        comments.write = { response.await(); Result.success(Unit) }
        val vm = vm()
        vm.onEvent(PhotoDetailEvent.OnCommentDraftChanged("submitted"))
        vm.onEvent(PhotoDetailEvent.OnWriteComment("submitted")); runCurrent()
        vm.onEvent(PhotoDetailEvent.OnCommentDraftChanged("new draft"))
        vm.onEvent(PhotoDetailEvent.OnWriteComment("new draft"))
        response.complete(Unit); advanceUntilIdle()
        assertEquals(listOf("submitted"), comments.writes)
        assertEquals("new draft", vm.uiState.value.commentDraft)
        assertEquals(PhotoDetailEffect.RefreshCommentPagingData, vm.effect.first())
        assertEquals(PhotoDetailEffect.ScrollCommentToBottom, vm.effect.first())
    }
    @Test fun editCommentCannotCloseAnotherDialogOrLoseFailedDraft() = runTest(main.testDispatcher) {
        val response = CompletableDeferred<Unit>()
        comments.edit = { response.await(); Result.success(Unit) }
        val vm = vm()
        vm.onEvent(PhotoDetailEvent.OnOpenCommentDialog(1, "one"))
        vm.onEvent(PhotoDetailEvent.OnEditComment(1, "saved")); runCurrent()
        vm.onEvent(PhotoDetailEvent.OnOpenCommentDialog(2, "two"))
        response.complete(Unit); advanceUntilIdle()
        assertEquals(PhotoCommentDialog.Edit(2, "two"), vm.uiState.value.commentDialog)
        comments.edit = { Result.failure(IOException("offline")) }
        vm.onEvent(PhotoDetailEvent.OnEditComment(2, "draft")); advanceUntilIdle()
        assertEquals(PhotoCommentDialog.Edit(2, "two"), vm.uiState.value.commentDialog)
    }
    @Test fun cancelledInitialReadIsNotErrorAndClearPreventsLateResults() = runTest(main.testDispatcher) {
        description.read = { Result.failure(CancellationException("cancelled")) }
        val vm = vm(); vm.initialize(); advanceUntilIdle()
        assertEquals(ScreenState.Loading, vm.uiState.value.screen)
        val response = CompletableDeferred<DescriptionItem>()
        description.read = { Result.success(response.await()) }
        vm.onEvent(PhotoDetailEvent.OnRetry); runCurrent()
        val before = vm.uiState.value
        store.clear(); response.complete(fixtureDescription); advanceUntilIdle()
        assertEquals(before, vm.uiState.value)
    }
}
