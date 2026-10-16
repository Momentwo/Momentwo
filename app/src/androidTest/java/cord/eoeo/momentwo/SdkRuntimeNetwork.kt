package cord.eoeo.momentwo

import android.net.ConnectivityManager
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals

/** Optional CI precondition: airplane settings alone do not prove that the app is offline. */
internal fun awaitSdkRuntimeNetwork() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val arguments = InstrumentationRegistry.getArguments()
    val offline = arguments.getString("requireOffline") == "true"
    val online = arguments.getString("requireOnline") == "true"
    if (!offline && !online) return
    require(offline != online)
    val manager = instrumentation.targetContext.getSystemService(ConnectivityManager::class.java)
    val expectedOnline = online
    val deadline = SystemClock.elapsedRealtime() + 15_000
    while ((manager.activeNetwork != null) != expectedOnline && SystemClock.elapsedRealtime() < deadline) {
        SystemClock.sleep(100)
    }
    assertEquals("SDK runtime connectivity precondition", expectedOnline, manager.activeNetwork != null)
}
