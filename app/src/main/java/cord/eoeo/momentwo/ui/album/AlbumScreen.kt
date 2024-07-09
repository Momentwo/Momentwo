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
import cord.eoeo.momentwo.ui.SIDE_EFFECTS_KEY
import cord.eoeo.momentwo.ui.album.composable.AlbumItemCard
import cord.eoeo.momentwo.ui.model.AlbumItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbunScreen(
    coroutineScope: CoroutineScope,
    uiState: () -> AlbumContract.State,
    effectFlow: () -> Flow<AlbumContract.Effect>,
    onEvent: (event: AlbumContract.Event) -> Unit,
    drawerState: () -> DrawerState,
    snackbarHostState: () -> SnackbarHostState,
    onClickDrawer: () -> Unit,
    onCloseDrawer: () -> Unit,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(SIDE_EFFECTS_KEY) {
        effectFlow().onEach { effect ->
            when (effect) {
                is AlbumContract.Effect.CloseDrawer -> {
                    onCloseDrawer()
                }

                is AlbumContract.Effect.PopBackStack -> {
                    popBackStack()
                }

                is AlbumContract.Effect.ShowSnackbar -> {
                    snackbarHostState().showSnackbar(
                        message = effect.message,
                    )
                }
            }
        }.collect()
    }

    BackHandler {
        onEvent(AlbumContract.Event.OnBack(drawerState().isOpen))
    }

    // 테스트용 가짜 앨범 아이템 리스트
    val fakeItems = listOf(
        AlbumItem(1, "Album1"),
        AlbumItem(2, "Album2"),
        AlbumItem(3, "Album3"),
        AlbumItem(4, "Album4"),
        AlbumItem(5, "Album5"),
        AlbumItem(6, "Album6"),
        AlbumItem(7, "Album7"),
        AlbumItem(8, "Album8"),
        AlbumItem(9, "Album9"),
        AlbumItem(10, "Album10"),
        AlbumItem(11, "Album11"),
    )

    ModalNavigationDrawer(
        drawerState = drawerState(),
        drawerContent = { AlbumDrawerContent() },
    ) {
        Scaffold(
            topBar = {
                AlbumTopAppBar(
                    onClickDrawer = onClickDrawer,
                    onClickSearch = { /*TODO: Navigate to Search*/ },
                )
            },
            floatingActionButton = { CreateAlbumFAB { /*TODO: Navigate to Create Album*/ } },
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
                items(items = fakeItems, key = { it.id }) { albumItem ->
                    AlbumItemCard(
                        albumItem = { albumItem },
                        modifier = Modifier.animateItemPlacement(),
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumDrawerContent() {
    ModalDrawerSheet {
        NavigationDrawerItem(
            label = { Text(text = "Profile", maxLines = 1) },
            icon = { Icon(Icons.Default.AccountCircle, "") },
            selected = false,
            onClick = { /*TODO: Navigate to Profile*/ },
            modifier = Modifier.padding(16.dp, 8.dp),
        )

        HorizontalDivider(modifier = Modifier.padding(16.dp, 0.dp))

        NavigationDrawerItem(
            label = { Text(text = "Setting", maxLines = 1) },
            icon = { Icon(Icons.Default.Settings, "") },
            selected = false,
            onClick = { /*TODO: Navigate to Setting*/ },
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
