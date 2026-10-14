package cord.eoeo.momentwo

import android.view.WindowManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.EntryPointAccessors
import cord.eoeo.momentwo.core.network.ApiDiagnostics
import cord.eoeo.momentwo.ui.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GuestAlbumCrudSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun ready() {
        compose.runOnUiThread { compose.activity.setShowWhenLocked(true); compose.activity.setTurnScreenOn(true); compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
        compose.waitUntil(10_000) { compose.activity.hasWindowFocus() }
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("내 로컬 앨범")).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
    }
    @Test fun albumAndSubAlbumCanBeCreatedEditedReopenedAndDeletedWithoutApi() {
        val counter = EntryPointAccessors.fromApplication(compose.activity.applicationContext, ApiDiagnostics::class.java).apiRequests()
        val before = counter.count
        compose.onNodeWithText("앨범 추가").performClick()
        compose.onNodeWithText("이름").performTextInput("Guest fixture")
        compose.onNodeWithText("저장").performClick()
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("서브앨범 추가")).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.onNodeWithText("서브앨범 추가").performClick()
        compose.onNodeWithText("이름").performTextInput("Guest child")
        compose.onNodeWithText("저장").performClick()
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("Guest child")).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("저장")).fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty() }
        compose.onNodeWithText("앨범 편집").performClick()
        compose.onNodeWithText("소제목").performTextInput("Persisted subtitle")
        compose.onNodeWithText("저장").performClick()
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("Persisted subtitle")).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("저장")).fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty() }
        compose.onNodeWithText("뒤로").performClick()
        compose.onNodeWithText("Guest fixture").performClick()
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("Guest child")).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.onNodeWithText("앨범 삭제").performClick()
        compose.onNode(hasText("삭제") and hasAnyAncestor(isDialog())).performClick()
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("내 로컬 앨범")).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.onNodeWithText("Guest fixture").assertDoesNotExist()
        assertEquals(before, counter.count)
    }
}
