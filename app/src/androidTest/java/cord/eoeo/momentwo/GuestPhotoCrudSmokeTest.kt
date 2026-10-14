package cord.eoeo.momentwo
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.core.net.toUri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cord.eoeo.momentwo.core.data.library.LibraryTestAccess
import cord.eoeo.momentwo.core.network.ApiDiagnostics
import cord.eoeo.momentwo.ui.MainActivity
import dagger.hilt.android.EntryPointAccessors
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class GuestPhotoCrudSmokeTest{
    @get:Rule val compose=createEmptyComposeRule()
    private lateinit var scenario:ActivityScenario<MainActivity>
    @Test fun captionMemoFavoriteAndImmersiveWorkWithoutApiAndPersistAfterActivityRestart()=runBlocking{
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val access=EntryPointAccessors.fromApplication(context,LibraryTestAccess::class.java)
        val repo=access.repository();val title="Photos-${UUID.randomUUID()}";val album=repo.createAlbum(title);val sub=repo.createSubAlbum(album,"My photos")
        val source=File(context.cacheDir,"fixture.png");Bitmap.createBitmap(12,12,Bitmap.Config.ARGB_8888).also{b->source.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()};val photo=repo.importPhoto(sub,source.toUri());source.delete()
        val counter=EntryPointAccessors.fromApplication(context,ApiDiagnostics::class.java).apiRequests();val before=counter.count
        try{
            fun open(){scenario=ActivityScenario.launch(MainActivity::class.java);scenario.onActivity{it.setShowWhenLocked(true);it.setTurnScreenOn(true);it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)};compose.waitUntil(10_000){compose.onAllNodes(hasText(title)).fetchSemanticsNodes(atLeastOneRootRequired=false).isNotEmpty()};compose.onNodeWithText(title).performClick();compose.waitUntil(10_000){compose.onAllNodes(hasText("My photos")).fetchSemanticsNodes(atLeastOneRootRequired=false).isNotEmpty()};compose.onNodeWithText("My photos").performClick();compose.waitUntil(10_000){compose.onAllNodes(hasContentDescription("사진 열기")).fetchSemanticsNodes(atLeastOneRootRequired=false).isNotEmpty()};compose.onNodeWithContentDescription("사진 열기").performClick();compose.waitUntil(10_000){compose.onAllNodes(hasText("즐겨찾기")).fetchSemanticsNodes(atLeastOneRootRequired=false).isNotEmpty()}}
            open()
            compose.onNodeWithText("즐겨찾기").performClick();compose.waitUntil(10_000){compose.onAllNodes(hasText("즐겨찾기 해제")).fetchSemanticsNodes(atLeastOneRootRequired=false).isNotEmpty()}
            compose.onNodeWithText("설명 편집").performScrollTo().performClick();compose.onNodeWithText("설명").performTextInput("Offline caption");compose.onNodeWithText("저장").performClick();compose.waitUntil(10_000){compose.onAllNodes(hasText("저장")).fetchSemanticsNodes(atLeastOneRootRequired=false).isEmpty()}
            compose.onNodeWithText("메모 추가").performScrollTo().performClick();compose.onNodeWithText("메모").performTextInput("Private memo");compose.onNodeWithText("저장").performClick();compose.waitUntil(10_000){compose.onAllNodes(hasText("저장")).fetchSemanticsNodes(atLeastOneRootRequired=false).isEmpty()}
            compose.onNodeWithText("몰입 모드").performScrollTo().performClick();compose.onNodeWithContentDescription("몰입 모드 닫기").performClick()
            scenario.recreate();scenario.onActivity{it.setShowWhenLocked(true);it.setTurnScreenOn(true);it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)};compose.waitUntil(10_000){compose.onAllNodes(hasText("Offline caption")).fetchSemanticsNodes(atLeastOneRootRequired=false).isNotEmpty()};compose.onNodeWithText("Offline caption").performScrollTo().assertExists();compose.onNodeWithText("Private memo").performScrollTo().assertExists()
            assertEquals(before,counter.count);assertTrue(repo.photo(photo).first()!!.favorite)
        }finally{if(::scenario.isInitialized)scenario.close();repo.deleteAlbum(album)}
    }
}
