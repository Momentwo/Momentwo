package cord.eoeo.momentwo

import android.view.WindowManager
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.network.ApiRequestCounter
import cord.eoeo.momentwo.ui.MainActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GuestEntrySmokeTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun firstEntryOpensDeviceLibraryWithoutAuthenticationOrApiCalls() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val counter = EntryPointAccessors.fromApplication(context, cord.eoeo.momentwo.core.network.ApiDiagnostics::class.java).apiRequests()
        val before = counter.count
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity {
                it.setShowWhenLocked(true); it.setTurnScreenOn(true)
                it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            compose.waitUntil(10_000) { compose.onAllNodes(androidx.compose.ui.test.hasText("Album")).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
            compose.onNodeWithText("Album").assertExists()
            compose.onNodeWithText("Momentwo").assertDoesNotExist()
            scenario.moveToState(Lifecycle.State.CREATED); scenario.moveToState(Lifecycle.State.RESUMED)
            compose.waitForIdle()
            assertEquals(before, counter.count)
        }
    }
}
