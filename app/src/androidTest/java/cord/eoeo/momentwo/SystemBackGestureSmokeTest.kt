package cord.eoeo.momentwo

import android.os.ParcelFileDescriptor
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.WindowManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.network.ApiDiagnostics
import cord.eoeo.momentwo.ui.MainActivity
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CI enables gesture navigation; this drives the system edge gesture, including the real IME. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 35)
class SystemBackGestureSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun waitFor(condition: () -> Boolean) = compose.waitUntil(10_000, condition)
    private fun hasTextNode(text: String) = compose.onAllNodesWithText(text)
        .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
    private fun imeVisible() = ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
        ?.isVisible(WindowInsetsCompat.Type.ime()) == true
    private fun systemBackGesture() {
        val coordinates = compose.runOnIdle {
            val view = compose.activity.window.decorView
            val origin = IntArray(2)
            view.getLocationOnScreen(origin)
            listOf(origin[0] + 1, origin[1] + view.height / 2, origin[0] + view.width * 3 / 4)
        }
        val (startX, y, endX) = coordinates
        val output = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("input swipe $startX $y $endX $y 500")
        ParcelFileDescriptor.AutoCloseInputStream(output).use { it.readBytes() }
    }

    @Test fun systemBackHidesImeBeforeLeavingCreateAlbumAndPreservesInput() {
        val pendingImeAnimations = AtomicInteger()
        compose.runOnUiThread {
            val activity = compose.activity
            activity.setShowWhenLocked(true)
            activity.setTurnScreenOn(true)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.window.decorView.setWindowInsetsAnimationCallback(object : WindowInsetsAnimation.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                override fun onPrepare(animation: WindowInsetsAnimation) {
                    if (animation.typeMask and WindowInsets.Type.ime() != 0) pendingImeAnimations.incrementAndGet()
                }
                override fun onEnd(animation: WindowInsetsAnimation) {
                    if (animation.typeMask and WindowInsets.Type.ime() != 0) pendingImeAnimations.decrementAndGet()
                }
                override fun onProgress(insets: WindowInsets, runningAnimations: MutableList<WindowInsetsAnimation>) = insets
            })
        }
        val counter = EntryPointAccessors.fromApplication(compose.activity, ApiDiagnostics::class.java).apiRequests()
        val before = counter.count
        try {
            waitFor { hasTextNode("Album") && compose.activity.hasWindowFocus() }
            compose.onNodeWithContentDescription("앨범 추가").performClick()
            waitFor { hasTextNode("Create Album") }
            val text = "system-gesture-draft"
            compose.onNode(hasSetTextAction()).performClick().performTextReplacement(text)
            waitFor { imeVisible() && pendingImeAnimations.get() == 0 }
            systemBackGesture()
            waitFor { !imeVisible() && pendingImeAnimations.get() == 0 }
            compose.onNodeWithText("Create Album").assertExists()
            compose.onNode(hasSetTextAction()).assertTextEquals(text)
            systemBackGesture()
            waitFor { hasTextNode("Album") && !hasTextNode("Create Album") }
            assertEquals(before, counter.count)
        } finally {
            compose.runOnUiThread { compose.activity.window.decorView.setWindowInsetsAnimationCallback(null) }
        }
    }
}
