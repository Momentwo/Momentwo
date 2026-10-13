package cord.eoeo.momentwo.core.common

import org.junit.Assert.*
import org.junit.Test

class ScreenStateTest {
    @Test fun firstFailureDoesNotExposeData() {
        assertEquals(ScreenState.Error(UiError.Network), ScreenState.Loading.failRefresh(UiError.Network))
        assertEquals(ScreenState.Loading, ScreenState.Error(UiError.Network).startRefresh())
    }
    @Test fun refreshFailurePreservesSuccessfulEmptyContent() {
        val state = ScreenState.Success(emptyList<Int>())
        val failed = state.startRefresh().failRefresh(UiError.Network) as ScreenState.Success
        assertEquals(emptyList<Int>(), failed.content)
        assertEquals(RefreshState.Error(UiError.Network), failed.refresh)
    }
    @Test fun cancellationOnlyClearsRefreshProgress() {
        val content = listOf(1)
        val state = ScreenState.Success(content, RefreshState.Loading).cancelRefresh() as ScreenState.Success
        assertSame(content, state.content)
        assertEquals(RefreshState.Idle, state.refresh)
    }
}
