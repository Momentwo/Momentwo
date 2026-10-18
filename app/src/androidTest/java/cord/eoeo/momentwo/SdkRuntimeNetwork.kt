package cord.eoeo.momentwo

import android.app.Activity
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

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

/** Check the activity's effective configuration, not only the emulator's requested wm settings. */
internal fun assertSdkRuntimeDisplay(activity: Activity) {
    if (InstrumentationRegistry.getArguments().getString("requireLargeLandscape") != "true") return
    val configuration = activity.resources.configuration
    assertTrue("Expected sw >= 600dp: $configuration", configuration.smallestScreenWidthDp >= 600)
    assertEquals("Expected landscape activity", Configuration.ORIENTATION_LANDSCAPE, configuration.orientation)
    assertEquals("Expected 1.3x font scale", 1.3f, configuration.fontScale, 0.01f)
}
