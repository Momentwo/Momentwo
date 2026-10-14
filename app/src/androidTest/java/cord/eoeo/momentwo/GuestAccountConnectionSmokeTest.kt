package cord.eoeo.momentwo
import android.view.WindowManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import cord.eoeo.momentwo.core.network.ApiDiagnostics
import cord.eoeo.momentwo.ui.MainActivity
import dagger.hilt.android.EntryPointAccessors
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class GuestAccountConnectionSmokeTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun cancellingOptionalLoginReturnsToDeviceLibraryWithoutApiOrUpload(){
        compose.runOnUiThread{compose.activity.setShowWhenLocked(true);compose.activity.setTurnScreenOn(true);compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
        fun wait(text:String){compose.waitUntil(10_000){compose.onAllNodes(hasText(text)).fetchSemanticsNodes(atLeastOneRootRequired=false).isNotEmpty()}}
        wait("내 로컬 앨범");val counter=EntryPointAccessors.fromApplication(compose.activity,ApiDiagnostics::class.java).apiRequests();val before=counter.count
        compose.onNodeWithText("계정 연결").performClick();wait("계정·저장 공간");compose.onNodeWithText("계정 없이 사용 중").assertExists()
        compose.onNodeWithText("로그인").performClick();wait("Momentwo");compose.runOnUiThread{compose.activity.onBackPressedDispatcher.onBackPressed()}
        wait("계정·저장 공간");compose.onNodeWithText("뒤로").performClick();wait("내 로컬 앨범");assertEquals(before,counter.count)
    }
}
