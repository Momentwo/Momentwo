package cord.eoeo.momentwo
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
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
        awaitSdkRuntimeNetwork()
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val access=EntryPointAccessors.fromApplication(context,LibraryTestAccess::class.java)
        val repo=access.repository();val title="Photos-${UUID.randomUUID()}";val album=repo.createAlbum(title);val sub=repo.createSubAlbum(album,"My photos")
        val source=File(context.cacheDir,"fixture.png");Bitmap.createBitmap(12,12,Bitmap.Config.ARGB_8888).also{b->source.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()};val photo=repo.importPhoto(sub,source.toUri());source.delete()
        val counter=EntryPointAccessors.fromApplication(context,ApiDiagnostics::class.java).apiRequests();val before=counter.count
        try{
            fun wait(predicate: SemanticsMatcher) { compose.waitUntil(10_000){compose.onAllNodes(predicate).fetchSemanticsNodes(atLeastOneRootRequired=false).isNotEmpty()} }
            fun closeSheet(title: String) {
                // Use the sheet's accessibility dismiss action: a Back key may be consumed by IME.
                // The header can disappear before the dialog window stops intercepting touches.
                compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss) and hasText(title))
                    .performSemanticsAction(SemanticsActions.Dismiss)
                compose.waitUntil(10_000) {
                    compose.onAllNodes(hasText(title)).fetchSemanticsNodes(atLeastOneRootRequired=false).isEmpty() &&
                        compose.onAllNodes(isDialog(), useUnmergedTree=true)
                            .fetchSemanticsNodes(atLeastOneRootRequired=false).isEmpty()
                }
                compose.waitUntil(10_000) {
                    var ready = false
                    scenario.onActivity {
                        val insets = ViewCompat.getRootWindowInsets(it.window.decorView)
                        ready = it.hasWindowFocus() && insets != null &&
                            !insets.isVisible(WindowInsetsCompat.Type.ime())
                    }
                    ready
                }
            }
            scenario=ActivityScenario.launch(MainActivity::class.java);scenario.onActivity{assertSdkRuntimeDisplay(it);it.setShowWhenLocked(true);it.setTurnScreenOn(true);it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
            wait(hasText(title))
            compose.onNodeWithText(title).performClick()
            wait(hasText("My photos"))
            compose.onNodeWithText("My photos").performClick()
            wait(hasContentDescription("사진"))
            compose.onNodeWithContentDescription("사진").performClick()
            wait(hasContentDescription("즐겨찾기"))
            compose.onNodeWithContentDescription("즐겨찾기").performClick();compose.waitUntil(10_000){runBlocking{repo.photo(photo).first()!!.favorite}}
            compose.onNodeWithContentDescription("사진 설명").performClick();compose.onNodeWithContentDescription("설명 작성").performClick()
            compose.onNodeWithText("제목 입력").performTextInput("Offline caption");compose.onNodeWithText("내용 입력").performTextInput("Offline body\nsecond line")
            compose.onNodeWithContentDescription("확인").performClick();wait(hasText("Offline caption"));compose.waitUntil(10_000){runBlocking{repo.photo(photo).first()!!.description=="Offline caption\nOffline body\nsecond line"}}
            closeSheet("설명")
            compose.onNodeWithContentDescription("개인 메모").performClick();compose.onNodeWithText("개인 메모 입력").performTextInput("Private memo");compose.onNodeWithContentDescription("개인 메모 작성").performClick();wait(hasText("Private memo"));closeSheet("개인 메모")
            compose.onNodeWithContentDescription("사진").performClick()
            compose.waitUntil(10_000){compose.onAllNodes(hasContentDescription("즐겨찾기")).fetchSemanticsNodes(atLeastOneRootRequired=false).isEmpty()}
            compose.onNodeWithContentDescription("사진").performClick()
            wait(hasContentDescription("즐겨찾기"))
            scenario.recreate();scenario.onActivity{assertSdkRuntimeDisplay(it);it.setShowWhenLocked(true);it.setTurnScreenOn(true);it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)};wait(hasContentDescription("즐겨찾기"))
            compose.onNodeWithContentDescription("사진 설명").performClick();wait(hasText("Offline caption"));compose.onNodeWithText("Offline caption").assertExists();closeSheet("설명")
            compose.onNodeWithContentDescription("개인 메모").performClick();wait(hasText("Private memo"));compose.onNodeWithText("Private memo").assertExists()
            assertEquals(before,counter.count);assertTrue(repo.photo(photo).first()!!.favorite)
        }finally{if(::scenario.isInitialized)scenario.close();repo.deleteAlbum(album)}
    }
}
