package cord.eoeo.momentwo.ui.friend

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cord.eoeo.momentwo.ui.SIDE_EFFECTS_KEY
import cord.eoeo.momentwo.ui.friend.friendlist.FriendListScreen
import cord.eoeo.momentwo.ui.friend.friendrequest.FriendRequestRoute
import cord.eoeo.momentwo.ui.model.BottomNavigationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendScreen(
    coroutineScope: CoroutineScope,
    navController: NavHostController,
    navActions: FriendNavigationActions,
    uiState: () -> FriendContract.State,
    effectFlow: () -> Flow<FriendContract.Effect>,
    onEvent: (event: FriendContract.Event) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        effectFlow()
            .onEach { effect ->
                when (effect) {
                    is FriendContract.Effect.PopBackStack -> {
                        popBackStack()
                    }

                    is FriendContract.Effect.ShowSnackbar -> {
                        snackbarHostState().showSnackbar(
                            message = effect.message,
                        )
                    }
                }
            }.collect()
    }

    val items =
        listOf(
            BottomNavigationItem(
                title = "친구 목록",
                selectedIcon = Icons.Default.Groups,
                unselectedIcon = Icons.Outlined.Groups,
                onClick = navActions.navigateToFriendList,
            ),
            BottomNavigationItem(
                title = "친구 요청",
                selectedIcon = Icons.Default.NotificationAdd,
                unselectedIcon = Icons.Outlined.NotificationAdd,
                onClick = navActions.navigateToFriendRequest,
            ),
        )

    Scaffold(
        topBar = {
            FriendTopAppBar(
                selectedNavIndex = { uiState().selectedNavIndex },
                onClickBack = { onEvent(FriendContract.Event.OnBack) },
                onClickAdd = { /* TODO: Open Add Friend Dialog */ },
            )
        },
        bottomBar = {
            FriendNavigationBar(
                items = { items },
                selectedNavIndex = { uiState().selectedNavIndex },
                onChangeNavIndex = { onEvent(FriendContract.Event.OnChangeNavIndex(it)) },
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = FriendDestination.LIST_ROUTE,
            ) {
                composable(route = FriendDestination.LIST_ROUTE) {
                    FriendListScreen(
                        friendList = { uiState().friendList },
                        isFriendListChanged = { uiState().isFriendListChanged },
                        getFriendList = { onEvent(FriendContract.Event.getFriendList) },
                    )
                }
                composable(route = FriendDestination.REQUEST_ROUTE) {
                    FriendRequestRoute(
                        coroutineScope = coroutineScope,
                        receivedRequests = { uiState().receivedRequests },
                        sentRequests = { uiState().sentRequests },
                        isReceivedListChanged = { uiState().isReceivedListChanged },
                        isSentListChanged = { uiState().isSentListChanged },
                        getReceivedRequests = { onEvent(FriendContract.Event.getReceivedRequests) },
                        getSentRequests = { onEvent(FriendContract.Event.getSentRequests) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendTopAppBar(
    selectedNavIndex: () -> Int,
    onClickBack: () -> Unit,
    onClickAdd: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = if (selectedNavIndex() == 0) "친구 목록" else "친구 요청") },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "")
            }
        },
        actions = {
            IconButton(onClick = onClickAdd) {
                Icon(Icons.Default.PersonAdd, "")
            }
        },
    )
}

@Composable
fun FriendNavigationBar(
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
                        contentDescription = "",
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
