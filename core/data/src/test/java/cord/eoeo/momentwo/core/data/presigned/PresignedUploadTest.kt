package cord.eoeo.momentwo.core.data.presigned

import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PresignedUploadTest {
    private val url = "https://storage.example/album/fixture.png?signature=test"
    @Test fun successfulUploadCommitsTheOriginalObjectKey() = runTest {
        val calls = mutableListOf<String>()
        val result = commitPresignedUpload(
            requestUrl = { calls += "url"; Result.success(url) },
            upload = { calls += it; Result.success(Unit) },
            commit = { calls += it; Result.success(Unit) })
        assertTrue(result.isSuccess)
        assertEquals(listOf("url", url, "album/fixture.png"), calls)
    }
    @Test fun failedUploadKeepsOriginalCauseAndDoesNotCommit() = runTest {
        val failure = IOException("offline")
        var committed = false
        val result = commitPresignedUpload({ Result.success(url) }, { Result.failure(failure) }, { committed = true; Result.success(Unit) })
        assertSame(failure, result.exceptionOrNull())
        assertFalse(committed)
    }
    @Test fun nestedCancellationAtEveryStepIsRethrown() = runTest {
        for (cancelledStep in 0..2) {
            var reached = -1
            suspend fun <T> step(index: Int, value: T): Result<T> {
                reached = index
                return if (index == cancelledStep) Result.failure(CancellationException("dependency cancelled")) else Result.success(value)
            }
            try {
                commitPresignedUpload({ step(0, url) }, { step(1, Unit) }, { step(2, Unit) })
                fail("Expected cancellation")
            } catch (_: CancellationException) { }
            assertEquals(cancelledStep, reached)
        }
    }
    @Test fun dependencySwallowingCancellationCannotStartNextRemoteWrite() = runTest {
        var upload = false
        val child = launch {
            commitPresignedUpload(
                requestUrl = { currentCoroutineContext().cancel(); Result.success(url) },
                upload = { upload = true; Result.success(Unit) }, commit = { fail("No commit"); Result.success(Unit) })
        }
        child.join()
        assertTrue(child.isCancelled)
        assertFalse(upload)
    }
    @Test fun fatalErrorIsNotConvertedToFailure() = runTest {
        val fatal = AssertionError("fatal")
        try { commitPresignedUpload({ Result.success(url) }, { Result.failure(fatal) }, { Result.success(Unit) }); fail("Expected fatal") }
        catch (actual: AssertionError) { assertSame(fatal, actual) }
    }
}
