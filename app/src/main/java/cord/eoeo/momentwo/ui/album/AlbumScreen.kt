package cord.eoeo.momentwo.ui.album

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import cord.eoeo.momentwo.ui.SIDE_EFFECTS_KEY
import cord.eoeo.momentwo.ui.album.composable.AlbumItemCard
import cord.eoeo.momentwo.ui.model.AlbumItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    uiState: () -> AlbumContract.State,
    effectFlow: () -> Flow<AlbumContract.Effect>,
    onEvent: (event: AlbumContract.Event) -> Unit,
    drawerState: () -> DrawerState,
    snackbarHostState: () -> SnackbarHostState,
    onClickDrawer: () -> Unit,
    onCloseDrawer: () -> Unit,
    navigateToCreateAlbum: () -> Unit,
    navigateToFriend: () -> Unit,
    navigateToAlbumDetail: (AlbumItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        onEvent(AlbumContract.Event.OnGetAlbumList)

        effectFlow().onEach { effect ->
            when (effect) {
                is AlbumContract.Effect.CloseDrawer -> {
                    onCloseDrawer()
                }

                is AlbumContract.Effect.ShowSnackbar -> {
                    snackbarHostState().showSnackbar(
                        message = effect.message,
                    )
                }
            }
        }.collect()
    }

    BackHandler(drawerState().isOpen) {
        onEvent(AlbumContract.Event.OnCloseDrawer)
    }

    ModalNavigationDrawer(
        drawerState = drawerState(),
        drawerContent = {
            AlbumDrawerContent(
                navigateToProfile = { /*TODO: Navigate to Profile*/ },
                navigateToFriend = navigateToFriend,
                navigateToSetting = { /*TODO: Navigate to Setting*/ },
            )
        },
    ) {
        Scaffold(
            topBar = {
                AlbumTopAppBar(
                    onClickDrawer = onClickDrawer,
                    onClickSearch = { /*TODO: Navigate to Search*/ },
                )
            },
            floatingActionButton = { CreateAlbumFAB(onClick = navigateToCreateAlbum) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState()) },
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = paddingValues,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp, 0.dp),
            ) {
                items(items = uiState().albumList, key = { it.id }) { albumItem ->
                    AlbumItemCard(
                        imageLoader = imageLoader,
                        albumItem = { albumItem },
                        navigateToAlbumDetail = { navigateToAlbumDetail(albumItem) },
                        modifier = Modifier.animateItemPlacement(),
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumDrawerContent(
    navigateToProfile: () -> Unit,
    navigateToFriend: () -> Unit,
    navigateToSetting: () -> Unit
) {
    ModalDrawerSheet {
        NavigationDrawerItem(
            label = { Text(text = "Profile", maxLines = 1) },
            icon = { Icon(Icons.Default.AccountCircle, "") },
            selected = false,
            onClick = navigateToProfile,
            modifier = Modifier.padding(16.dp, 8.dp),
        )

        HorizontalDivider(modifier = Modifier.padding(16.dp, 0.dp))

        NavigationDrawerItem(
            label = { Text(text = "Friends", maxLines = 1) },
            icon = { Icon(Icons.Default.Groups, "") },
            selected = false,
            onClick = navigateToFriend,
            modifier = Modifier.padding(16.dp, 8.dp),
        )
        NavigationDrawerItem(
            label = { Text(text = "Setting", maxLines = 1) },
            icon = { Icon(Icons.Default.Settings, "") },
            selected = false,
            onClick = navigateToSetting,
            modifier = Modifier.padding(16.dp, 8.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumTopAppBar(
    onClickDrawer: () -> Unit,
    onClickSearch: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(text = "Album") },
        navigationIcon = {
            IconButton(onClick = onClickDrawer) {
                Icon(Icons.Default.Menu, "")
            }
        },
        actions = {
            IconButton(onClick = onClickSearch) {
                Icon(Icons.Default.Search, "")
            }
        },
    )
}

@Composable
fun CreateAlbumFAB(
    onClick: () -> Unit
) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, "")
    }
}
