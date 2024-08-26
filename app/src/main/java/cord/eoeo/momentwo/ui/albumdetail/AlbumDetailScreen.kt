package cord.eoeo.momentwo.ui.albumdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PhotoAlbum
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.ImageLoader
import cord.eoeo.momentwo.ui.SIDE_EFFECTS_KEY
import cord.eoeo.momentwo.ui.albumdetail.albumsetting.AlbumSettingRoute
import cord.eoeo.momentwo.ui.albumdetail.member.MemberScreen
import cord.eoeo.momentwo.ui.albumdetail.subalbumlist.SubAlbumListScreen
import cord.eoeo.momentwo.ui.model.BottomNavigationItem
import cord.eoeo.momentwo.ui.model.MemberAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    coroutineScope: CoroutineScope,
    navController: NavHostController,
    navActions: AlbumDetailNavigationActions,
    scrollBehavior: TopAppBarScrollBehavior,
    imageLoader: ImageLoader,
    uiState: () -> AlbumDetailContract.State,
    effectFlow: () -> Flow<AlbumDetailContract.Effect>,
    onEvent: (event: AlbumDetailContract.Event) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        effectFlow()
            .onEach { effect ->
                when (effect) {
                    is AlbumDetailContract.Effect.PopBackStack -> {
                        popBackStack()
                    }

                    is AlbumDetailContract.Effect.ShowSnackbar -> {
                        snackbarHostState().showSnackbar(
                            message = effect.message,
                        )
                    }
                }
            }.collect()
    }

    val navItems =
        listOf(
            BottomNavigationItem(
                title = "앨범",
                selectedIcon = Icons.Default.PhotoAlbum,
                unselectedIcon = Icons.Outlined.PhotoAlbum,
                onClick = navActions.navigateToSubAlbumList,
            ),
            BottomNavigationItem(
                title = "멤버",
                selectedIcon = Icons.Default.People,
                unselectedIcon = Icons.Outlined.People,
                onClick = navActions.navigateToMember,
            ),
            BottomNavigationItem(
                title = "설정",
                selectedIcon = Icons.Default.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                onClick = navActions.navigateToAlbumSetting,
            ),
        )

    Scaffold(
        topBar = {
            AlbumDetailTopAppBar(
                title = {
                    "Title" // TODO: 앨범 화면에서 AlbumItem 데이터 가져오기
                },
                subTitle = {
                    "Sub Title" // TODO: 앨범 화면에서 AlbumItem 데이터 가져오기
                },
                onClickBack = { onEvent(AlbumDetailContract.Event.OnBack) },
                onClickAdd = { /* TODO: Sub Album 추가 */ },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            AlbumDetailBottomNavigation(
                items = { navItems },
                selectedNavIndex = { uiState().selectedNavIndex },
                onChangeNavIndex = { onEvent(AlbumDetailContract.Event.OnChangeNavIndex(it)) },
            )
        },
        modifier =
            Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize(),
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AlbumDetailDestination.SUB_ALBUM_LIST_ROUTE,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            composable(route = AlbumDetailDestination.SUB_ALBUM_LIST_ROUTE) {
                SubAlbumListScreen()
            }
            composable(route = AlbumDetailDestination.MEMBER_ROUTE) {
                MemberScreen()
            }
            composable(route = AlbumDetailDestination.ALBUM_SETTING_ROUTE) {
                AlbumSettingRoute(memberAuth = { MemberAuth.ADMIN })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailTopAppBar(
    title: () -> String,
    subTitle: () -> String,
    onClickBack: () -> Unit,
    onClickAdd: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    LargeTopAppBar(
        title = {
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = title(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (scrollBehavior.isPinned.not()) {
                    Text(
                        text = subTitle(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "")
            }
        },
        actions = {
            IconButton(onClick = onClickAdd) {
                Icon(Icons.Default.Add, "")
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun AlbumDetailBottomNavigation(
    items: () -> List<BottomNavigationItem>,
    selectedNavIndex: () -> Int,
    onChangeNavIndex: (Int) -> Unit,
) {
    NavigationBar {
        items().forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selectedNavIndex() == index) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                    )
                },
                label = { Text(text = item.title) },
                selected = selectedNavIndex() == index,
                onClick = {
                    if (selectedNavIndex() != index) {
                        onChangeNavIndex(index)
                        item.onClick()
                    }
                },
            )
        }
    }
}
