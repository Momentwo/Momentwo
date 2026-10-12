package cord.eoeo.momentwo

import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.performClick
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.imageLoader
import cord.eoeo.momentwo.core.designsystem.theme.MomentwoTheme
import cord.eoeo.momentwo.feature.friend.api.FriendDestination
import cord.eoeo.momentwo.feature.auth.api.LoginDestination
import cord.eoeo.momentwo.feature.auth.api.ProfileDestination
import cord.eoeo.momentwo.feature.auth.api.SignUpDestination
import cord.eoeo.momentwo.feature.albumdetail.api.AlbumDetailDestination
import cord.eoeo.momentwo.feature.album.api.AlbumDestination
import cord.eoeo.momentwo.feature.album.api.CreateAlbumDestination
import cord.eoeo.momentwo.ui.MainActivity
import cord.eoeo.momentwo.ui.SubAlbumContentAdapter
import cord.eoeo.momentwo.feature.albumdetail.impl.SubAlbumContentState
import cord.eoeo.momentwo.feature.albumdetail.impl.SubAlbumContentActions
import cord.eoeo.momentwo.core.model.SubAlbumItem
import cord.eoeo.momentwo.ui.MomentwoNavGraph
import cord.eoeo.momentwo.ui.MomentwoNavigationActions
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the real app graph/Hilt/SavedStateHandle without requiring a live API. */
@RunWith(AndroidJUnit4::class)
class AppNavigationSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var navController: NavHostController

    @Before
    fun installTestGraph() {
        compose.runOnUiThread {
            val activity = compose.activity
            // Display only this test activity when the device auto-locks during the suite.
            activity.setShowWhenLocked(true)
            activity.setTurnScreenOn(true)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val images = activity.applicationContext.imageLoader
            val insets = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            activity.setContent {
                val scope = rememberCoroutineScope()
                navController = rememberNavController()
                val actions = MomentwoNavigationActions(navController)
                MomentwoTheme {
                    MomentwoNavGraph(scope, navController, actions, images, insets)
                }
            }
        }
        // Waking a locked device is asynchronous; wait until this window can receive input.
        compose.waitUntil(timeoutMillis = 10_000) { compose.activity.hasWindowFocus() }
        compose.waitForIdle()
    }

    @Test
    fun loginSignUpAndBackUseFeatureGraph() {
        compose.onNodeWithText("회원가입").performClick()
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(SignUpDestination, navController.currentBackStackEntry!!.toRoute<SignUpDestination>())
        }
        compose.onNodeWithText("다음").assertExists()
        compose.runOnIdle { navController.popBackStack() }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(LoginDestination, navController.currentBackStackEntry!!.toRoute<LoginDestination>())
        }
        compose.onNodeWithText("Momentwo").assertExists()
    }

    @Test
    fun nullableProfileArgumentsReachHiltViewModel() {
        compose.runOnIdle { navController.navigate(ProfileDestination(null)) }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(ProfileDestination(null), navController.currentBackStackEntry!!.toRoute<ProfileDestination>())
        }
        compose.runOnIdle { navController.popBackStack() }
        compose.waitForIdle()
        compose.onNodeWithText("Momentwo").assertExists()
    }

    @Test
    fun friendGraphAndRequestTabAreReachableWithoutBackend() {
        compose.runOnIdle { navController.navigate(FriendDestination) }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(FriendDestination, navController.currentBackStackEntry!!.toRoute<FriendDestination>())
        }
        compose.onNodeWithText("친구 요청").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("받은 요청").assertExists()
        compose.runOnIdle { navController.popBackStack() }
        compose.waitForIdle()
        compose.onNodeWithText("Momentwo").assertExists()
    }
    @Test
    fun subAlbumSlotAndDetailTabsAreReachableWithoutBackend() {
        val destination = AlbumDetailDestination(123, "Migration album", "Test", "")
        compose.runOnIdle { navController.navigate(destination) }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(destination, navController.currentBackStackEntry!!.toRoute<AlbumDetailDestination>()) }
        compose.onNodeWithText("Migration album").assertExists()
        compose.onNodeWithText("서브 앨범").assertExists()
        compose.onNodeWithText("멤버").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("설정").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("서브 앨범").performClick()
        compose.waitForIdle()
        compose.runOnIdle { navController.popBackStack() }
        compose.waitForIdle()
        compose.onNodeWithText("Momentwo").assertExists()
    }

    @Test
    fun subAlbumAdapterPreservesClickAndEditSelectionCallbacks() {
        val state = mutableStateOf(SubAlbumContentState(listOf(SubAlbumItem(7, "Fixture", emptyList())), false, emptyList()))
        var clicked: Pair<Int, String>? = null
        var selected: Pair<Boolean, Int>? = null
        var loads = 0
        compose.runOnUiThread {
            val images = compose.activity.applicationContext.imageLoader
            compose.activity.setContent {
                MomentwoTheme {
                    SubAlbumContentAdapter(images, state.value, SubAlbumContentActions(
                        getSubAlbums = { loads++ },
                        onClickItem = { id, title -> clicked = id to title },
                        onChangeSelected = { checked, id -> selected = checked to id },
                        onBack = {},
                    ))
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Fixture").assertExists()
        compose.onAllNodes(hasClickAction())[0].performClick()
        compose.runOnIdle {
            assertEquals(7 to "Fixture", clicked)
            org.junit.Assert.assertTrue(loads > 0)
            clicked = null
            state.value = state.value.copy(isEditMode = true)
        }
        compose.waitForIdle()
        compose.onAllNodes(hasClickAction())[0].performClick()
        compose.runOnIdle {
            assertEquals(true to 7, selected)
            assertEquals(null, clicked)
        }
    }

    @Test
    fun albumAndCreateAlbumGraphsRestoreTheirBackStack() {
        compose.runOnIdle { navController.navigate(AlbumDestination) }
        compose.waitForIdle()
        compose.onNodeWithText("Album").assertExists()
        compose.runOnIdle { navController.navigate(CreateAlbumDestination) }
        compose.waitForIdle()
        compose.onNodeWithText("앨범명").assertExists()
        compose.runOnIdle {
            assertEquals(CreateAlbumDestination, navController.currentBackStackEntry!!.toRoute<CreateAlbumDestination>())
            navController.popBackStack()
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(AlbumDestination, navController.currentBackStackEntry!!.toRoute<AlbumDestination>())
            navController.popBackStack()
        }
        compose.waitForIdle()
        compose.onNodeWithText("Momentwo").assertExists()
    }

}
