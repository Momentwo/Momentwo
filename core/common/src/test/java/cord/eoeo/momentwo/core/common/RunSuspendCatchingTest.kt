package cord.eoeo.momentwo.core.common

import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RunSuspendCatchingTest {
    @Test fun ordinaryFailureIsReturned() = runTest {
        val failure = IOException("offline")
        assertSame(failure, runSuspendCatching<Int> { throw failure }.exceptionOrNull())
    }

    @Test fun cancellationIsRethrown() = runTest {
        val cancellation = CancellationException("request replaced")
        try {
            runSuspendCatching<Unit> { throw cancellation }
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) { assertSame(cancellation, actual) }
    }

    @Test fun fatalErrorIsRethrown() = runTest {
        val error = AssertionError("broken invariant")
        try {
            runSuspendCatching<Unit> { throw error }
            fail("Error must propagate")
        } catch (actual: AssertionError) { assertSame(error, actual) }
    }

    @Test fun cancelledContextDoesNotExecuteBlock() = runTest {
        var called = false
        val cancelled = Job().apply { cancel() }
        CoroutineScope(coroutineContext + cancelled).launch(start = CoroutineStart.UNDISPATCHED) {
            runSuspendCatching { called = true }
        }.join()
        assertFalse(called)
    }

    @Test fun swallowedCancellationCannotReturnSuccess() = runTest {
        var success = false
        val request = launch(start = CoroutineStart.UNDISPATCHED) {
            runSuspendCatching {
                currentCoroutineContext().cancel()
                try { yield() } catch (_: CancellationException) { /* faulty dependency */ }
                42
            }.onSuccess { success = true }
        }
        request.join()
        assertTrue(request.isCancelled)
        assertFalse(success)
    }

    @Test fun suspendedRequestCancellationDoesNotRunFailureHandler() = runTest {
        var failure = false
        var success = false
        val request = launch(start = CoroutineStart.UNDISPATCHED) {
            runSuspendCatching { awaitCancellation() }
                .onFailure { failure = true }
                .onSuccess { success = true }
        }
        request.cancelAndJoin()
        assertFalse(failure)
        assertFalse(success)
    }
}
