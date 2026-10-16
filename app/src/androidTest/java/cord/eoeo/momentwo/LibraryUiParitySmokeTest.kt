package cord.eoeo.momentwo

import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.imageLoader
import cord.eoeo.momentwo.core.data.library.LibraryTestAccess
import cord.eoeo.momentwo.core.database.*
import cord.eoeo.momentwo.core.designsystem.theme.MomentwoTheme
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.network.ApiDiagnostics
import cord.eoeo.momentwo.feature.album.api.*
import cord.eoeo.momentwo.feature.albumdetail.api.LocalAlbumDetailDestination
import cord.eoeo.momentwo.feature.photo.api.*
import cord.eoeo.momentwo.ui.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Actual UUID Routes with identical persisted fixtures, in both themes. No test-only production screen. */
@RunWith(AndroidJUnit4::class)
class LibraryUiParitySmokeTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private var generation=0
    private lateinit var navigation: NavHostController
    private fun waitFor(matcher: SemanticsMatcher) { compose.waitUntil(10_000) { compose.onAllNodes(matcher).fetchSemanticsNodes(atLeastOneRootRequired=false).isNotEmpty() } }
    private fun capture(name: String) {
        compose.mainClock.advanceTimeBy(600)
        compose.waitForIdle()
        // Compose idleness does not wait for the OS window compositor/dialog transition.
        android.os.SystemClock.sleep(400)
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val output=File(compose.activity.filesDir,"ui-parity").apply{mkdirs()}
        val image=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("Cannot capture the visible test window")
        File(output,"$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG,100,it) }
    }
    private fun show(dark: Boolean, destination: Any) {
        val token=++generation
        compose.runOnUiThread {
            val activity=compose.activity
            activity.setShowWhenLocked(true);activity.setTurnScreenOn(true);activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            WindowCompat.getInsetsController(activity.window,activity.window.decorView).isAppearanceLightStatusBars=!dark
            activity.setContent { key(token) {
                val scope=rememberCoroutineScope();navigation=rememberNavController()
                MomentwoTheme(darkTheme=dark) { MomentwoNavGraph(scope,navigation,MomentwoNavigationActions(navigation),activity.applicationContext.imageLoader,WindowCompat.getInsetsController(activity.window,activity.window.decorView)) }
            } }
        }
        waitFor(hasText("Album"))
        compose.runOnIdle { if(destination!=LocalLibraryDestination)navigation.navigate(destination) }
        compose.waitForIdle()
    }
    @Test fun deviceAndAccountRoutesShareControlsInLightAndDarkWithoutContentRequests()=runBlocking {
        val app=compose.activity.applicationContext
        val access=EntryPointAccessors.fromApplication(app,LibraryTestAccess::class.java)
        val db=access.database()
        val repo=access.repository();val prefs=access.preferences()
        prefs.clearSession()
        val id=UUID.randomUUID().toString();val device=repo.createAlbum("Parity album", "device-$id")
        repo.editAlbumSubtitle(device,"Same fixture")
        val sub=repo.createSubAlbum(device,"Parity sub", "device-sub-$id")
        val source=File(app.cacheDir,"parity-source.png")
        Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888).also { bitmap -> bitmap.eraseColor(android.graphics.Color.rgb(44,116,164));source.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle() }
        val photo=repo.importPhoto(sub,android.net.Uri.fromFile(source),"device-photo-$id");repo.setCover(device,android.net.Uri.fromFile(source));source.delete()
        repo.setDescription(photo,"Title\nBody\nSecond line");repo.setFavorite(photo,true);repo.saveMemo(photo,"Private record")
        val name=requireNotNull(repo.photo(photo).first()!!.mediaName)
        val account="ui-parity-$id";prefs.storeSession(account,"fixture-access","fixture-refresh")
        val identity=prefs.getSession().getOrThrow()!!.identity;prefs.expireSession(account,identity.epoch)
        val remote="account-album-$id";val remoteSub="account-sub-$id";val remotePhoto="account-photo-$id"
        db.libraryDao().insertLibrary(LibraryEntity("account:$account",account,account))
        db.libraryDao().insertAlbum(LocalAlbumEntity(remote,"account:$account","Parity album",subtitle="Same fixture",coverMediaName=db.libraryDao().album(device)!!.coverMediaName,remoteId=901,createdAt=1,role=MemberAuth.ADMIN.roleString,subsLoaded=true))
        db.libraryDao().insertSubAlbum(LocalSubAlbumEntity(remoteSub,remote,"Parity sub",remoteId=902,createdAt=1,photosLoaded=true))
        db.libraryDao().insertPhoto(LocalPhotoEntity(remotePhoto,remoteSub,name,remoteId=903,description="Title\nBody\nSecond line",descriptionKnown=true,favorite=true,isLiked=true,likeCount=5,likesKnown=true,commentsLoaded=true,createdAt=1))
        repo.saveMemo(remotePhoto,"Private record")
        val counter=EntryPointAccessors.fromApplication(app,ApiDiagnostics::class.java).apiRequests();val before=counter.count
        try {
            for(dark in listOf(false,true))for(member in listOf(false,true)) {
                val prefix="${if(dark)"dark"else "light"}-${if(member)"account"else "device"}"
                val albumId=if(member)remote else device;val subId=if(member)remoteSub else sub;val photoId=if(member)remotePhoto else photo
                show(dark,if(member)AccountAlbumsDestination else LocalLibraryDestination)
                waitFor(hasText("Parity album"));compose.onNodeWithContentDescription("앨범 추가").assertExists();capture("$prefix-albums")
                compose.runOnIdle { navigation.navigate(LibraryCreateAlbumDestination(member)) }
                waitFor(hasSetTextAction() and isEnabled());compose.onNode(hasSetTextAction()).performTextReplacement("Parity draft");androidx.test.espresso.Espresso.closeSoftKeyboard();compose.onNodeWithText("앨범명").assertExists();capture("$prefix-create")
                if(member) {
                    prefs.storeSession("other-$account","other-access","other-refresh")
                    waitFor(hasText("작성하던 계정으로 다시 연결하면 이어서 만들 수 있습니다"))
                    compose.onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.EditableText)).assertIsNotEnabled().assertTextEquals("")
                    prefs.storeSession(account,"new-access","new-refresh")
                    val restored=prefs.getSession().getOrThrow()!!.identity;prefs.expireSession(account,restored.epoch)
                    waitFor(hasSetTextAction() and isEnabled());compose.onNode(hasSetTextAction()).assertTextEquals("Parity draft")
                }
                compose.runOnIdle { navigation.navigate(LocalAlbumDetailDestination(albumId)) }
                waitFor(hasText("Parity sub"));compose.onNodeWithText("멤버").assertExists();compose.onNodeWithText("설정").assertExists();capture("$prefix-detail")
                compose.onNodeWithText("설정").performClick();waitFor(hasText("앨범 제목 수정"));compose.onNodeWithText("앨범 부제목 수정").assertExists();if(member)compose.onNodeWithText("앨범 나가기").assertExists()else compose.onNodeWithText("앨범 나가기").assertDoesNotExist();capture("$prefix-settings")
                compose.onNodeWithText("대표 이미지 수정").performClick();waitFor(hasContentDescription("앨범 이미지 수정"));capture("$prefix-cover")
                if(member) {
                    val originalSub=db.libraryDao().subAlbum(subId)!!;val originalPhoto=db.libraryDao().photo(photoId)!!
                    db.libraryDao().updateSubAlbum(originalSub.copy(photosLoaded=false));db.libraryDao().updatePhoto(originalPhoto.copy(deleted=true))
                    compose.runOnIdle { navigation.navigate(LocalPhotoListDestination(subId)) }
                    waitFor(hasText("Parity sub"));compose.onNodeWithText("Parity album").assertExists();compose.onNodeWithContentDescription("사진").assertDoesNotExist()
                    db.libraryDao().updatePhoto(originalPhoto);db.libraryDao().updateSubAlbum(originalSub)
                    compose.runOnIdle { navigation.popBackStack() }
                }
                compose.runOnIdle { navigation.navigate(LocalPhotoListDestination(subId)) }
                waitFor(hasContentDescription("사진"));compose.onNodeWithContentDescription("사진 추가").assertExists();compose.onNodeWithContentDescription("더보기").assertExists();capture("$prefix-grid")
                compose.runOnIdle { navigation.navigate(LocalPhotoDetailDestination(photoId)) }
                waitFor(hasContentDescription(if(member)"좋아요"else "즐겨찾기"));capture("$prefix-viewer")
                compose.onNodeWithContentDescription("사진 설명").performClick();waitFor(hasText("Title"));capture("$prefix-description")
                androidx.test.espresso.Espresso.pressBack();compose.waitForIdle()
                if(member) { compose.onNodeWithContentDescription("더보기").performClick();compose.onNodeWithText("개인 메모").performClick() }
                else compose.onNodeWithContentDescription("개인 메모").performClick()
                waitFor(hasText("Private record"));capture("$prefix-records")
                androidx.test.espresso.Espresso.pressBack();compose.waitForIdle()
            }
            assertEquals(before,counter.count)
        } finally {
            prefs.clearSession();db.libraryDao().deleteAlbum(remote);repo.deleteAlbum(device)
        }
    }
}
