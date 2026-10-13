package cord.eoeo.momentwo.feature.album.impl.album

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import cord.eoeo.momentwo.core.ui.CollectEffect
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.core.ui.EmptyContent
import kotlinx.coroutines.launch
import cord.eoeo.momentwo.core.ui.AlbumItemCard
import cord.eoeo.momentwo.core.model.AlbumItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@Composable
internal fun AlbumScreen(
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    uiState: () -> AlbumState,
    effectFlow: () -> Flow<AlbumEffect>,
    onEvent: (event: AlbumEvent) -> Unit,
    drawerState: () -> DrawerState,
    snackbarHostState: () -> SnackbarHostState,
    onClickDrawer: () -> Unit,
    navigateToCreateAlbum: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToFriend: () -> Unit,
    navigateToAlbumDetail: (AlbumItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    CollectEffect(effectFlow()) { effect ->
        when (effect) {
            AlbumEffect.CloseDrawer -> launch { drawerState().close() }
            is AlbumEffect.ShowSnackbar -> launch { snackbarHostState().showSnackbar(effect.message) }
        }
    }

    BackHandler(drawerState().isOpen) {
        onEvent(AlbumEvent.OnCloseDrawer)
    }

    ModalNavigationDrawer(
        drawerState = drawerState(),
        drawerContent = {
            AlbumDrawerContent(
                navigateToProfile = navigateToProfile,
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
            ScreenContent(uiState().screen, { onEvent(AlbumEvent.OnGetAlbumList) }, Modifier.padding(paddingValues)) { content ->
                if (content.albums.isEmpty()) EmptyContent() else
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp, 0.dp),
            ) {
                items(items = content.albums, key = { it.id }) { albumItem ->
                    AlbumItemCard(
                        imageLoader = imageLoader,
                        image = { albumItem.imageUrl },
                        title = { albumItem.title },
                        subTitle = { albumItem.subTitle },
                        onClick = { navigateToAlbumDetail(albumItem) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            }
        }
    }
}

@Composable
internal fun AlbumDrawerContent(
    navigateToProfile: () -> Unit,
    navigateToFriend: () -> Unit,
    navigateToSetting: () -> Unit,
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
internal fun AlbumTopAppBar(
    onClickDrawer: () -> Unit,
    onClickSearch: () -> Unit,
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
internal fun CreateAlbumFAB(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, "")
    }
}
