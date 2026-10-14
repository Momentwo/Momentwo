package cord.eoeo.momentwo

import android.view.WindowManager
import androidx.compose.material3.Text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import cord.eoeo.momentwo.core.ui.rememberPagingScreenState
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.common.RefreshState
import cord.eoeo.momentwo.core.common.UiError
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.core.ui.CollectEffect
import cord.eoeo.momentwo.core.ui.SystemBarsEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.CopyOnWriteArrayList
import androidx.activity.compose.setContent
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
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
import cord.eoeo.momentwo.feature.photo.api.PhotoListDestination
import cord.eoeo.momentwo.feature.photo.api.PhotoDetailDestination
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
        kotlinx.coroutines.runBlocking { access().preferences().clearSession() }
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
        // Focus can arrive before setContent's first composition registers its test root.
        compose.waitUntil(timeoutMillis = 10_000) {
            ::navController.isInitialized && navController.currentBackStackEntry != null && compose.activity.hasWindowFocus() &&
                compose.activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        compose.waitForIdle()
    }

    @org.junit.After
    fun clearFixtureAccount() { kotlinx.coroutines.runBlocking { access().preferences().clearSession() } }

    private fun access() = dagger.hilt.android.EntryPointAccessors.fromApplication(compose.activity.applicationContext,cord.eoeo.momentwo.core.data.library.LibraryTestAccess::class.java)
    private fun useExpiredFixtureAccount() = kotlinx.coroutines.runBlocking {
        val preferences=access().preferences()
        preferences.storeSession("navigation-fixture","fixture-access","fixture-refresh")
        val identity=preferences.getSession().getOrThrow()!!.identity
        preferences.expireSession(identity.accountId,identity.epoch)
        // A verified, expired identity admits cached navigation but cannot send a content request.
    }

    @Test
    fun loginSignUpAndBackUseFeatureGraph() {
        compose.onNodeWithText("계정 연결").performClick()
        compose.onNodeWithText("로그인").performClick()
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
        compose.onNodeWithText("내 로컬 앨범").assertExists()
    }

    @Test
    fun friendGraphAndRequestTabAreReachableWithoutBackend() {
        useExpiredFixtureAccount()
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
        compose.onNodeWithText("내 로컬 앨범").assertExists()
    }
    @Test
    fun subAlbumSlotAndDetailTabsAreReachableWithoutBackend() {
        useExpiredFixtureAccount()
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
        compose.onNodeWithText("내 로컬 앨범").assertExists()
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
        useExpiredFixtureAccount()
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
        compose.onNodeWithText("내 로컬 앨범").assertExists()
    }

    @Test
    fun photoArgumentsInitialStateAndBackStackArePreserved() {
        useExpiredFixtureAccount()
        val list = PhotoListDestination(123, 456, "Migration album", "Migration subalbum")
        val detail = PhotoDetailDestination(123, 789, "", false)
        compose.runOnIdle { navController.navigate(list) }
        compose.waitForIdle()
        compose.onNodeWithText("Migration subalbum").assertExists()
        compose.runOnIdle { navController.navigate(detail) }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(detail, navController.currentBackStackEntry!!.toRoute<PhotoDetailDestination>()) }
        // Required API data is unavailable: no placeholder photo/actions may be shown.
        compose.onNodeWithContentDescription("사진 설명").assertDoesNotExist()
        compose.onNodeWithContentDescription("사진").assertDoesNotExist()
        compose.runOnIdle { navController.popBackStack() }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(list, navController.currentBackStackEntry!!.toRoute<PhotoListDestination>())
            navController.popBackStack()
        }
        compose.waitForIdle()
        compose.onNodeWithText("내 로컬 앨범").assertExists()
    }

    @Test
    fun screenContentHidesIncompleteDataAndRetainsRefreshContent() {
        val screen = mutableStateOf<ScreenState<String>>(ScreenState.Loading)
        compose.runOnUiThread {
            compose.activity.setContent { MomentwoTheme { ScreenContent(screen.value, {}) { Text(it) } } }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Fixture content").assertDoesNotExist()
        compose.runOnIdle { screen.value = ScreenState.Success("Fixture content") }
        compose.onNodeWithText("Fixture content").assertExists()
        compose.runOnIdle { screen.value = ScreenState.Success("Fixture content", RefreshState.Loading) }
        compose.onNodeWithText("Fixture content").assertExists()
        compose.runOnIdle { screen.value = ScreenState.Success("Fixture content", RefreshState.Error(UiError.Network)) }
        compose.onNodeWithText("Fixture content").assertExists()
        compose.onNodeWithText("다시 시도").assertExists()
        compose.runOnIdle { screen.value = ScreenState.Error(UiError.Unauthorized) }
        compose.onNodeWithText("Fixture content").assertDoesNotExist()
        compose.onNodeWithText("다시 시도").assertDoesNotExist()
    }

    @Test
    fun bufferedEffectsResumeInOrderWithoutRecompositionDuplicates() {
        val channel = Channel<Int>(Channel.BUFFERED)
        val flow = channel.receiveAsFlow()
        val delivered = CopyOnWriteArrayList<Int>()
        val generation = mutableStateOf(0)
        compose.runOnUiThread {
            compose.activity.setContent {
                val offset = generation.value * 10
                Text("generation ${generation.value}")
                CollectEffect(flow) { delivered.add(it + offset) }
            }
        }
        compose.waitForIdle()
        channel.trySend(1).getOrThrow()
        compose.waitUntil { delivered.toList() == listOf(1) }
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        channel.trySend(2).getOrThrow()
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertEquals(listOf(1), delivered.toList())
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.waitUntil { delivered.toList() == listOf(1, 2) }
        compose.runOnIdle { generation.value = 1 }
        // The new handler exists only after this composition has committed.
        compose.waitForIdle()
        compose.onNodeWithText("generation 1").assertExists()
        channel.trySend(3).getOrThrow()
        compose.waitUntil { delivered.toList() == listOf(1, 2, 13) }
        channel.close()
    }

    @Test
    fun systemBarsRestoreOnPauseAndDisposalAndReapplyOnResume() {
        val visible = mutableStateOf(false)
        val enabled = mutableStateOf(true)
        val controller = WindowCompat.getInsetsController(compose.activity.window, compose.activity.window.decorView)
        compose.runOnUiThread {
            compose.activity.setContent { if (enabled.value) SystemBarsEffect(controller, visible.value) }
        }
        compose.waitForIdle()
        compose.waitUntil(timeoutMillis = 10_000) { ViewCompat.getRootWindowInsets(compose.activity.window.decorView)?.isVisible(WindowInsetsCompat.Type.statusBars()) == false }
        compose.runOnIdle { assertEquals(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE, controller.systemBarsBehavior) }
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.onActivity { assertEquals(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT, controller.systemBarsBehavior) }
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.waitForIdle()
        compose.waitUntil(timeoutMillis = 10_000) { ViewCompat.getRootWindowInsets(compose.activity.window.decorView)?.isVisible(WindowInsetsCompat.Type.statusBars()) == false }
        compose.runOnIdle { assertEquals(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE, controller.systemBarsBehavior) }
        compose.runOnIdle { visible.value = true }
        compose.runOnIdle { assertEquals(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT, controller.systemBarsBehavior); visible.value = false }
        compose.runOnIdle { enabled.value = false }
        compose.waitUntil(timeoutMillis = 10_000) { ViewCompat.getRootWindowInsets(compose.activity.window.decorView)?.isVisible(WindowInsetsCompat.Type.statusBars()) == true }
        compose.runOnIdle { assertEquals(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT, controller.systemBarsBehavior) }
    }

    @Test
    fun pagingCollectsBehindLoadingAndKeepsSuccessfulEmptyBodyDuringRefresh() {
        val initial = CompletableDeferred<Unit>()
        val refreshed = CompletableDeferred<Unit>()
        val calls = AtomicInteger()
        val flow = Pager(PagingConfig(10)) {
            object : PagingSource<Int, String>() {
                override fun getRefreshKey(state: PagingState<Int, String>): Int? = null
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
                    if (calls.incrementAndGet() == 1) initial.await() else refreshed.await()
                    return LoadResult.Page(emptyList(), null, null)
                }
            }
        }.flow
        var refresh: () -> Unit = {}
        compose.runOnUiThread {
            compose.activity.setContent {
                val items = flow.collectAsLazyPagingItems()
                refresh = { items.refresh() }
                val state = rememberPagingScreenState(items) { UiError.Network }
                MomentwoTheme { ScreenContent(state, { items.retry() }) { Text("Empty paging fixture") } }
            }
        }
        compose.waitUntil(timeoutMillis = 10_000) { calls.get() == 1 }
        compose.onNodeWithText("Empty paging fixture").assertDoesNotExist()
        initial.complete(Unit)
        compose.waitUntil(timeoutMillis = 10_000) { compose.onAllNodes(androidx.compose.ui.test.hasText("Empty paging fixture")).fetchSemanticsNodes().isNotEmpty() }
        compose.runOnIdle { refresh() }
        compose.waitUntil(timeoutMillis = 10_000) { calls.get() == 2 }
        compose.onNodeWithText("Empty paging fixture").assertExists()
        refreshed.complete(Unit)
        compose.waitForIdle()
        compose.onNodeWithText("Empty paging fixture").assertExists()
    }

}
