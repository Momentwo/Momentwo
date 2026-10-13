package cord.eoeo.momentwo.core.common

import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RequestControllerTest {
    private enum class Key : RequestKey { First, Second }

    @Test fun latestRequestSuppressesOldResultAndOldCleanup() = runTest {
        val controller = RequestController(backgroundScope)
        val old = CompletableDeferred<Int>()
        val next = CompletableDeferred<Int>()
        val received = mutableListOf<Int>()
        var pending = false
        controller.launchRequest(Key.First, RequestPolicy.LatestWins,
            request = { try { Result.success(old.await()) } catch (_: CancellationException) { Result.success(-1) } },
            onStarted = { pending = true }, onSuccess = { received += it },
            onFailure = { fail("Unexpected error") }, onFinished = { pending = false })
        runCurrent()
        controller.launchRequest(Key.First, RequestPolicy.LatestWins,
            request = { Result.success(next.await()) }, onStarted = { pending = true },
            onSuccess = { received += it }, onFailure = { fail("Unexpected error") },
            onFinished = { pending = false })
        runCurrent()
        assertTrue(pending)
        assertTrue(received.isEmpty())
        next.complete(2)
        runCurrent()
        assertEquals(listOf(2), received)
        assertFalse(pending)
    }

    @Test fun duplicateWriteIsSkippedBeforeStartCallback() = runTest {
        val controller = RequestController(backgroundScope)
        val response = CompletableDeferred<Unit>()
        var starts = 0
        var calls = 0
        fun submit() = controller.launchRequest(Key.First, RequestPolicy.IgnoreWhileRunning,
            request = { calls++; response.await(); Result.success(Unit) },
            onStarted = { starts++ }, onSuccess = {}, onFailure = {})
        val first = submit()
        val duplicate = submit()
        runCurrent()
        assertNotNull(first)
        assertNull(duplicate)
        assertEquals(1, starts)
        assertEquals(1, calls)
        response.complete(Unit)
        runCurrent()
    }

    @Test fun differentKeysRunIndependently() = runTest {
        val controller = RequestController(backgroundScope)
        val response = CompletableDeferred<Unit>()
        var calls = 0
        for (key in Key.entries) controller.launchRequest(key, RequestPolicy.IgnoreWhileRunning,
            request = { calls++; response.await(); Result.success(Unit) }, onSuccess = {}, onFailure = {})
        runCurrent()
        assertEquals(2, calls)
        response.complete(Unit)
        runCurrent()
    }

    @Test fun nestedCancellationIsNeverAFailureCallback() = runTest {
        val controller = RequestController(backgroundScope)
        var success = false
        var failure = false
        var finished = false
        val job = controller.launchRequest(Key.First, RequestPolicy.LatestWins,
            request = { Result.failure<Int>(CancellationException("cancelled dependency")) },
            onSuccess = { success = true }, onFailure = { failure = true }, onFinished = { finished = true })!!
        runCurrent()
        assertTrue(job.isCancelled)
        assertFalse(success)
        assertFalse(failure)
        assertTrue(finished)
    }

    @Test fun closeStopsCallbacksAndRejectsNewRequests() = runTest {
        val controller = RequestController(backgroundScope)
        var callback = false
        controller.launchRequest<Unit>(Key.First, RequestPolicy.LatestWins,
            request = { awaitCancellation() }, onSuccess = { callback = true },
            onFailure = { callback = true }, onFinished = { callback = true })
        runCurrent()
        controller.close()
        runCurrent()
        assertFalse(callback)
        assertNull(controller.launchRequest(Key.First, RequestPolicy.LatestWins,
            request = { Result.success(1) }, onSuccess = { callback = true }, onFailure = {}))
    }

    @Test fun successCallbackBugIsNotConvertedToRequestFailure() = runTest {
        var uncaught: Throwable? = null
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler) +
            CoroutineExceptionHandler { _, exception -> uncaught = exception })
        val controller = RequestController(scope)
        val bug = IllegalStateException("callback bug")
        var failures = 0
        controller.launchRequest(Key.First, RequestPolicy.LatestWins,
            request = { Result.success(1) }, onSuccess = { throw bug }, onFailure = { failures++ })
        runCurrent()
        assertSame(bug, uncaught)
        assertEquals(0, failures)
        scope.cancel()
    }

    @Test fun ordinaryFailureReachesOnlyFailureCallback() = runTest {
        val controller = RequestController(backgroundScope)
        val error = IOException("offline")
        var failure: Throwable? = null
        controller.launchRequest(Key.First, RequestPolicy.LatestWins,
            request = { Result.failure<Int>(error) }, onSuccess = { fail("Unexpected success") },
            onFailure = { failure = it })
        runCurrent()
        assertSame(error, failure)
    }
}
