package cord.eoeo.momentwo.ui.albumdetail

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import cord.eoeo.momentwo.ui.albumdetail.albumsetting.ChangeImageRoute
import cord.eoeo.momentwo.ui.albumdetail.member.MemberScreen
import cord.eoeo.momentwo.ui.albumdetail.subalbumlist.SubAlbumListScreen
import cord.eoeo.momentwo.ui.composable.InviteDialog
import cord.eoeo.momentwo.ui.composable.TextFieldDialog
import cord.eoeo.momentwo.ui.model.BottomNavigationItem
import cord.eoeo.momentwo.ui.model.MemberAuth
import cord.eoeo.momentwo.ui.model.TextFieldDialogItem
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

                    is AlbumDetailContract.Effect.PopBackStackInAlbumDetail -> {
                        navActions.popBackStack()
                    }

                    is AlbumDetailContract.Effect.ShowSnackbar -> {
                        snackbarHostState().showSnackbar(
                            message = effect.message,
                        )
                    }
                }
            }.collect()
    }

    BackHandler {
        onEvent(AlbumDetailContract.Event.OnBack)
    }

    val navItems =
        listOf(
            BottomNavigationItem(
                title = "서브 앨범",
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

    val textFieldDialogItems =
        listOf(
            TextFieldDialogItem(
                titleText = "서브앨범 생성",
                description = "새 서브앨범의 제목을 입력하세요",
                onConfirm = {
                    onEvent(
                        AlbumDetailContract.Event.OnSubAlbumEvents(
                            AlbumDetailContract.SubAlbumEvents.Create(
                                subAlbumTitle = it
                            )
                        )
                    )
                },
            ),
            TextFieldDialogItem(
                titleText = "제목 변경",
                description = "변경할 제목을 입력하세요",
                onConfirm = {
                    onEvent(
                        AlbumDetailContract.Event.OnAlbumSettingEvents(
                            AlbumDetailContract.AlbumSettingEvents.EditTitle(
                                title = it
                            )
                        )
                    )
                },
                initialText = uiState().albumItem.title,
            ),
            TextFieldDialogItem(
                titleText = "부제목 변경",
                description = "변경할 부제목을 입력하세요",
                onConfirm = {
                    onEvent(
                        AlbumDetailContract.Event.OnAlbumSettingEvents(
                            AlbumDetailContract.AlbumSettingEvents.EditSubTitle(
                                subTitle = it
                            )
                        )
                    )
                },
                initialText = uiState().albumItem.subTitle,
            ),
        )

    if (uiState().isInviteDialogOpened) {
        InviteDialog(
            friendItemList = { uiState().friendList },
            selectedFriendList = { uiState().selectedFriendList },
            onCheckedChange = { isChecked, friendItem ->
                onEvent(
                    AlbumDetailContract.Event.OnChangeFriendSelected(
                        isSelected = isChecked,
                        friendItem = friendItem,
                    )
                )
            },
            getIsChecked = { uiState().selectedFriendList.contains(it) },
            onDismiss = { onEvent(AlbumDetailContract.Event.OnDismissInviteFriend) },
            onConfirm = { onEvent(AlbumDetailContract.Event.OnConfirmInviteFriend) },
        )
    }

    if (uiState().textFieldDialogIndex >= 0) {
        val item = textFieldDialogItems[uiState().textFieldDialogIndex]

        TextFieldDialog(
            titleText = { item.titleText },
            description = { item.description },
            onDismiss = { onEvent(AlbumDetailContract.Event.OnDismissTextFieldDialog) },
            onConfirm = item.onConfirm,
        )
    }

    Scaffold(
        topBar = {
            AlbumDetailTopAppBar(
                title = { uiState().albumItem.title },
                subTitle = { uiState().albumItem.subTitle },
                selectedNavIndex = { uiState().selectedNavIndex },
                memberAuth = { uiState().permission },
                isEditMode = { uiState().isEditMode },
                onClickBack = {
                    if (uiState().isInChangeImage) {
                        onEvent(AlbumDetailContract.Event.OnChangeIsInChangeImage(false))
                        onEvent(AlbumDetailContract.Event.OnBackInAlbumDetail)
                    } else {
                        onEvent(AlbumDetailContract.Event.OnBack)
                    }
                },
                onClickAdd = { onEvent(AlbumDetailContract.Event.OnClickAdd) },
                onClickEdit = { onEvent(AlbumDetailContract.Event.OnClickEdit) },
                onClickCancel = { onEvent(AlbumDetailContract.Event.OnCancelEdit) },
                onClickConfirm = { onEvent(AlbumDetailContract.Event.OnConfirmEdit) },
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
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AlbumDetailDestination.SubAlbumList,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            composable<AlbumDetailDestination.SubAlbumList> {
                SubAlbumListScreen(
                    imageLoader = imageLoader,
                    subAlbumList = { uiState().subAlbumList },
                    isEditMode = { uiState().isEditMode },
                    getSubAlbums = { onEvent(AlbumDetailContract.Event.OnSubAlbumEvents(AlbumDetailContract.SubAlbumEvents.GetSubAlbums)) },
                    getIsSelected = { uiState().selectedSubAlbumIds.contains(it) },
                    onClickItem = { /*TODO: Navigate to PhotoList*/ },
                    onChangeSubAlbumSelected = { isSelected, subAlbumId ->
                        onEvent(
                            AlbumDetailContract.Event.OnChangeSubAlbumSelected(
                                isSelected = isSelected,
                                subAlbumId = subAlbumId
                            )
                        )
                    },
                    onBack = {
                        if (uiState().isEditMode) {
                            onEvent(AlbumDetailContract.Event.OnCancelEdit)
                        } else {
                            onEvent(AlbumDetailContract.Event.OnBack)
                        }
                    },
                )
            }

            composable<AlbumDetailDestination.Member> {
                MemberScreen(
                    memberList = { uiState().memberList },
                    isEditMode = { uiState().isEditMode },
                    getMembers = { onEvent(AlbumDetailContract.Event.OnMemberEvents(AlbumDetailContract.MemberEvents.GetMembers)) },
                    getFriends = { onEvent(AlbumDetailContract.Event.OnGetFriendList) },
                    getIsSelected = { uiState().selectedMemberNicknames.contains(it) },
                    onChangeMemberSelected = { isSelected, memberNickname ->
                        onEvent(
                            AlbumDetailContract.Event.OnChangeMemberSelected(
                                isSelected = isSelected,
                                memberNickname = memberNickname,
                            )
                        )
                    },
                    onBack = {
                        if (uiState().isEditMode) {
                            onEvent(AlbumDetailContract.Event.OnCancelEdit)
                        } else {
                            onEvent(AlbumDetailContract.Event.OnBack)
                        }
                    },
                )
            }

            composable<AlbumDetailDestination.AlbumSetting> {
                AlbumSettingRoute(
                    memberAuth = { uiState().permission },
                    onClickChangeImage = navActions.navigateToChangeImage,
                    onOpenTextFieldDialog = { onEvent(AlbumDetailContract.Event.OnOpenTextFieldDialog(it)) },
                    onDeleteAlbum = {
                        onEvent(AlbumDetailContract.Event.OnAlbumSettingEvents(AlbumDetailContract.AlbumSettingEvents.DeleteAlbum))
                        onEvent(AlbumDetailContract.Event.OnBackInAlbumDetail)
                    },
                    onExitAlbum = { onEvent(AlbumDetailContract.Event.OnMemberEvents(AlbumDetailContract.MemberEvents.Exit)) },
                    onBack = { onEvent(AlbumDetailContract.Event.OnBack) },
                )
            }

            composable<AlbumDetailDestination.ChangeImage> {
                ChangeImageRoute(
                    imageLoader = imageLoader,
                    imageUrl = { uiState().albumItem.imageUrl },
                    onChangeIsInChangeImage = { onEvent(AlbumDetailContract.Event.OnChangeIsInChangeImage(it)) },
                    onClickChange = { uri ->
                        uri?.let {
                            onEvent(
                                AlbumDetailContract.Event.OnAlbumSettingEvents(
                                    AlbumDetailContract.AlbumSettingEvents.ChangeImage(it)
                                )
                            )
                        }
                            ?: onEvent(
                                AlbumDetailContract.Event.OnAlbumSettingEvents(
                                    AlbumDetailContract.AlbumSettingEvents.DeleteImage
                                )
                            )
                    },
                    onBack = {
                        onEvent(AlbumDetailContract.Event.OnChangeIsInChangeImage(false))
                        onEvent(AlbumDetailContract.Event.OnBackInAlbumDetail)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailTopAppBar(
    title: () -> String,
    subTitle: () -> String,
    selectedNavIndex: () -> Int,
    memberAuth: () -> MemberAuth,
    isEditMode: () -> Boolean,
    onClickBack: () -> Unit,
    onClickAdd: () -> Unit,
    onClickEdit: () -> Unit,
    onClickCancel: () -> Unit,
    onClickConfirm: () -> Unit,
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
            if (isEditMode().not()) {
                if (selectedNavIndex() != 2) {
                    IconButton(onClick = onClickAdd) {
                        Icon(Icons.Default.Add, "")
                    }
                    if (memberAuth() != MemberAuth.MEMBER) {
                        IconButton(onClick = onClickEdit) {
                            Icon(Icons.Default.Settings, "")
                        }
                    }
                }
            } else {
                TextButton(onClick = onClickCancel) {
                    Text(text = "취소", color = Color.DarkGray)
                }
                TextButton(onClick = onClickConfirm) {
                    Text(text = if (selectedNavIndex() == 0) "삭제" else "추방", color = MaterialTheme.colorScheme.error)
                }
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
