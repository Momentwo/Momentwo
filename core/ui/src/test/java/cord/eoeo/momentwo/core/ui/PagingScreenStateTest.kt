package cord.eoeo.momentwo.core.ui

import androidx.paging.*
import cord.eoeo.momentwo.core.common.*
import java.io.IOException
import org.junit.Assert.*
import org.junit.Test

class PagingScreenStateTest {
    private val done = LoadState.NotLoading(true)
    private fun states(source: LoadState, remote: LoadState? = null): CombinedLoadStates {
        val local = LoadStates(source, done, done)
        val mediator = remote?.let { LoadStates(it, done, done) }
        return CombinedLoadStates(remote ?: source, done, done, local, mediator)
    }
    private fun render(states: CombinedLoadStates, count: Int = 0, presented: Boolean = false) =
        pagingScreenState(states, count, presented) { UiError.Network }

    @Test fun initialLoadHidesContent() { assertEquals(ScreenState.Loading, render(states(LoadState.Loading))) }
    @Test fun completedEmptyPageIsSuccess() { assertEquals(ScreenState.Success(Unit), render(states(done, done))) }
    @Test fun emptySuccessRemainsVisibleDuringRefresh() {
        assertEquals(ScreenState.Success(Unit, RefreshState.Loading), render(states(LoadState.Loading), presented = true))
    }
    @Test fun cachedItemsAreVisibleWhileRemoteRefreshRuns() {
        assertEquals(ScreenState.Success(Unit, RefreshState.Loading), render(states(done, LoadState.Loading), count = 2))
    }
    @Test fun firstRemoteFailureWithoutCacheIsError() {
        assertEquals(ScreenState.Error(UiError.Network), render(states(done, LoadState.Error(IOException()))))
    }
    @Test fun remoteFailurePreservesCacheAndSuccessfulEmptyPage() {
        val failure = states(done, LoadState.Error(IOException()))
        val expected = ScreenState.Success(Unit, RefreshState.Error(UiError.Network))
        assertEquals(expected, render(failure, count = 2))
        assertEquals(expected, render(failure, presented = true))
    }
    @Test fun appendFailureDoesNotReplaceSuccessfulBodyWithInitialError() {
        val local = LoadStates(done, done, LoadState.Error(IOException("append")))
        val combined = CombinedLoadStates(done, done, local.append, local, null)
        assertEquals(ScreenState.Success(Unit), render(combined, count = 2, presented = true))
    }

}
