package cord.eoeo.momentwo.feature.friend.impl

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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.compose.LazyPagingItems
import coil.ImageLoader
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.ui.CollectEffect
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.core.data.toUiError
import kotlinx.coroutines.launch
import cord.eoeo.momentwo.core.ui.SearchUserDialog
import cord.eoeo.momentwo.feature.friend.impl.friendlist.FriendListScreen
import cord.eoeo.momentwo.feature.friend.impl.friendrequest.FriendRequestRoute
import cord.eoeo.momentwo.core.ui.BottomNavigationItem
import cord.eoeo.momentwo.core.model.FriendItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@Composable
internal fun FriendScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    navController: NavHostController,
    navActions: FriendNavigationActions,
    uiState: () -> FriendState,
    effectFlow: () -> Flow<FriendEffect>,
    onEvent: (event: FriendEvent) -> Unit,
    friendPagingData: () -> LazyPagingItems<FriendItem>,
    pagingState: () -> ScreenState<Unit>,
    retryPagingSource: () -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CollectEffect(effectFlow()) { effect ->
        when (effect) {
            FriendEffect.PopBackStack -> popBackStack()
            is FriendEffect.ShowSnackbar -> launch { snackbarHostState().showSnackbar(effect.message) }
        }
    }

    if (uiState().isRequestDialogOpened) {
        SearchUserDialog(
            imageLoader = imageLoader,
            searchResult = { uiState().searchUserPagingData },
            errorMapper = Throwable::toUiError,
            onQueryChanged = { onEvent(FriendEvent.OnSearchQueryChanged(it)) },
            onInteraction = { onEvent(FriendEvent.OnRequestDialogEdited) },
            onSearch = { onEvent(FriendEvent.OnGetSearchUser(it)) },
            onDismiss = { onEvent(FriendEvent.OnDismissRequestFriend) },
            onRequestFriend = { onEvent(FriendEvent.OnSendFriendRequest(it)) },
        )
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
                onClickBack = { onEvent(FriendEvent.OnBack) },
                onClickAdd = { onEvent(FriendEvent.OnClickRequestFriend) },
            )
        },
        bottomBar = {
            FriendNavigationBar(
                items = { items },
                selectedNavIndex = { uiState().selectedNavIndex },
                onChangeNavIndex = { onEvent(FriendEvent.OnChangeNavIndex(it)) },
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = FriendTabDestination.LIST_ROUTE,
            ) {
                composable(route = FriendTabDestination.LIST_ROUTE) {
                    ScreenContent(pagingState(), onRetry = {
                        if (uiState().friendPagingSource is ScreenState.Error) retryPagingSource() else friendPagingData().retry()
                    }) {
                        FriendListScreen(imageLoader, friendPagingData)
                    }
                }
                composable(route = FriendTabDestination.REQUEST_ROUTE) {
                    FriendRequestRoute(
                        coroutineScope = coroutineScope,
                        imageLoader = imageLoader,
                        uiState = uiState,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FriendTopAppBar(
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
internal fun FriendNavigationBar(
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
