package cord.eoeo.momentwo.core.data

import cord.eoeo.momentwo.core.common.UiError
import java.io.IOException
import kotlinx.coroutines.CancellationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class UiErrorMapperTest {
    @Test fun authenticationPermissionAndMissingTargetDoNotOfferBlindRetry() {
        for ((code, expected) in listOf(401 to UiError.Unauthorized, 403 to UiError.Forbidden, 404 to UiError.NotFound)) {
            val error = HttpException(Response.error<Unit>(code, "error".toResponseBody())).toUiError()
            assertEquals(expected, error)
            assertFalse(error.canRetry)
        }
    }
    @Test fun networkAndServerFailuresOfferRetry() {
        assertEquals(UiError.Network, IOException().toUiError())
        assertTrue(IOException().toUiError().canRetry)
        assertEquals(UiError.Server, HttpException(Response.error<Unit>(503, "error".toResponseBody())).toUiError())
        assertTrue(UiError.Server.canRetry)
    }
    @Test fun cancellationAndFatalErrorsAreNeverUserErrors() {
        val cancellation = CancellationException("cancelled")
        try { cancellation.toUiError(); fail("Expected cancellation") } catch (actual: CancellationException) { assertSame(cancellation, actual) }
        val fatal = AssertionError("fatal")
        try { fatal.toUiError(); fail("Expected fatal") } catch (actual: AssertionError) { assertSame(fatal, actual) }
    }
    @Test fun unclassifiedFailureUsesResourceBasedFallback() { assertEquals(UiError.Unknown, IllegalArgumentException().toUiError()) }
}
