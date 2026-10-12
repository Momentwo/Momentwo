package cord.eoeo.momentwo

import androidx.activity.compose.setContent
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
import cord.eoeo.momentwo.ui.MainActivity
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
}
