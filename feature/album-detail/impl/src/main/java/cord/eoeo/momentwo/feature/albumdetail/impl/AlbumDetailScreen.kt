package cord.eoeo.momentwo.feature.albumdetail.impl

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
import androidx.compose.runtime.key
import androidx.compose.runtime.Composable
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
import cord.eoeo.momentwo.core.ui.CollectEffect
import cord.eoeo.momentwo.core.ui.ScreenContent
import cord.eoeo.momentwo.core.ui.EmptyContent
import androidx.compose.ui.window.Dialog
import cord.eoeo.momentwo.feature.albumdetail.impl.albumsetting.AlbumSettingRoute
import cord.eoeo.momentwo.feature.albumdetail.impl.albumsetting.ChangeImageRoute
import cord.eoeo.momentwo.feature.albumdetail.impl.member.MemberScreen
import cord.eoeo.momentwo.feature.subalbum.api.SubAlbumListDestination
import cord.eoeo.momentwo.core.ui.InviteDialog
import cord.eoeo.momentwo.core.ui.TextFieldDialog
import cord.eoeo.momentwo.core.ui.BottomNavigationItem
import cord.eoeo.momentwo.core.model.MemberAuth
import cord.eoeo.momentwo.core.model.TextFieldDialogItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlbumDetailScreen(
    coroutineScope: CoroutineScope,
    navController: NavHostController,
    navActions: AlbumDetailNavigationActions,
    scrollBehavior: TopAppBarScrollBehavior,
    imageLoader: ImageLoader,
    uiState: () -> AlbumDetailState,
    effectFlow: () -> Flow<AlbumDetailEffect>,
    onEvent: (event: AlbumDetailEvent) -> Unit,
    snackbarHostState: () -> SnackbarHostState,
    popBackStack: () -> Unit,
    navigateToPhotoList: (Int, Int, String, String) -> Unit,
    subAlbumContent: @Composable (SubAlbumContentState, SubAlbumContentActions) -> Unit,
    modifier: Modifier = Modifier,
) {
    CollectEffect(effectFlow()) { effect ->
        when (effect) {
            AlbumDetailEffect.PopBackStack -> popBackStack()
            AlbumDetailEffect.PopBackStackInAlbumDetail -> navActions.popBackStack()
            is AlbumDetailEffect.ShowSnackbar -> launch { snackbarHostState().showSnackbar(effect.message) }
        }
    }

    BackHandler {
        onEvent(AlbumDetailEvent.OnBack)
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
                        AlbumDetailEvent.OnSubAlbumEvents(
                            SubAlbumEvent.Create(
                                subAlbumTitle = it,
                            ),
                        ),
                    )
                },
            ),
            TextFieldDialogItem(
                titleText = "제목 변경",
                description = "변경할 제목을 입력하세요",
                onConfirm = {
                    onEvent(
                        AlbumDetailEvent.OnAlbumSettingEvents(
                            AlbumSettingEvent.EditTitle(
                                title = it,
                            ),
                        ),
                    )
                },
                placeholder = uiState().albumItem.title,
            ),
            TextFieldDialogItem(
                titleText = "부제목 변경",
                description = "변경할 부제목을 입력하세요",
                onConfirm = {
                    onEvent(
                        AlbumDetailEvent.OnAlbumSettingEvents(
                            AlbumSettingEvent.EditSubTitle(
                                subTitle = it,
                            ),
                        ),
                    )
                },
                placeholder = uiState().albumItem.subTitle,
            ),
        )

    if (uiState().isInviteDialogOpened) {
        when (val friends = uiState().friends) {
            cord.eoeo.momentwo.core.common.ScreenState.Loading,
            is cord.eoeo.momentwo.core.common.ScreenState.Error -> Dialog(onDismissRequest = { onEvent(AlbumDetailEvent.OnDismissInviteFriend) }) {
                ScreenContent(friends, { onEvent(AlbumDetailEvent.OnGetFriendList) }, Modifier.fillMaxSize()) { }
            }
            is cord.eoeo.momentwo.core.common.ScreenState.Success -> InviteDialog(
                friendItemList = { friends.content }, selectedFriendList = { uiState().selectedFriendList },
                onCheckedChange = { selected, item -> onEvent(AlbumDetailEvent.OnChangeFriendSelected(selected, item)) },
                onDismiss = { onEvent(AlbumDetailEvent.OnDismissInviteFriend) },
                onConfirm = { onEvent(AlbumDetailEvent.OnConfirmInviteFriend) },
                getIsChecked = { item -> uiState().selectedFriendList.any { it.nickname == item.nickname } },
            )
        }
    }
    val dialogItem = when (uiState().dialog) {
        AlbumDetailDialog.None -> null
        AlbumDetailDialog.CreateSubAlbum -> textFieldDialogItems[0]
        AlbumDetailDialog.EditTitle -> textFieldDialogItems[1]
        AlbumDetailDialog.EditSubtitle -> textFieldDialogItems[2]
    }
    if (dialogItem != null) key(uiState().dialogSession) { TextFieldDialog(
        titleText = { dialogItem.titleText }, description = { dialogItem.description },
        onDismiss = { onEvent(AlbumDetailEvent.OnDismissTextFieldDialog) },
        onConfirm = dialogItem.onConfirm, placeholder = { dialogItem.placeholder },
        onValueChanged = { onEvent(AlbumDetailEvent.OnDialogEdited) },
    ) }

    Scaffold(
        topBar = {
            AlbumDetailTopAppBar(
                title = { uiState().albumItem.title },
                subTitle = { uiState().albumItem.subTitle },
                selectedNavIndex = { uiState().selectedNavIndex },
                canInteract = { uiState().permission != null },
                canEdit = { uiState().permission in listOf(MemberAuth.ADMIN, MemberAuth.SUB_ADMIN) },
                isEditMode = { uiState().isEditMode },
                isInChangeImage = { uiState().isInChangeImage },
                onClickBack = {
                    if (uiState().isInChangeImage) {
                        onEvent(AlbumDetailEvent.OnBackInAlbumDetail)
                    } else {
                        onEvent(AlbumDetailEvent.OnBack)
                    }
                },
                onClickAdd = { onEvent(AlbumDetailEvent.OnClickAdd) },
                onClickEdit = { onEvent(AlbumDetailEvent.OnClickEdit) },
                onClickCancel = { onEvent(AlbumDetailEvent.OnCancelEdit) },
                onClickConfirm = { onEvent(AlbumDetailEvent.OnConfirmEdit) },
                onClickChangeImage = {
                    onEvent(
                        AlbumDetailEvent.OnAlbumSettingEvents(
                            AlbumSettingEvent.ChangeImage,
                        ),
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            AlbumDetailBottomNavigation(
                items = { navItems },
                selectedNavIndex = { uiState().selectedNavIndex },
                onChangeNavIndex = { onEvent(AlbumDetailEvent.OnChangeNavIndex(it)) },
                onChangeIsInChangeImage = { onEvent(AlbumDetailEvent.OnChangeIsInChangeImage(false)) },
            )
        },
        modifier =
            Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize(),
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = SubAlbumListDestination,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            composable<SubAlbumListDestination> {
                ScreenContent(uiState().subAlbums, { onEvent(AlbumDetailEvent.OnRetryTab(DetailTab.SubAlbums)) }) { content ->
                subAlbumContent(
                    SubAlbumContentState(
                        items = content.items,
                        isEditMode = uiState().isEditMode,
                        selectedIds = uiState().selectedSubAlbumIds,
                    ),
                    SubAlbumContentActions(
                        getSubAlbums = { onEvent(AlbumDetailEvent.OnSubAlbumEvents(SubAlbumEvent.GetSubAlbums)) },
                        onClickItem = { subAlbumId, subAlbumTitle ->
                            onEvent(AlbumDetailEvent.OnSubAlbumVisited)
                            navigateToPhotoList(uiState().albumItem.id, subAlbumId, uiState().albumItem.title, subAlbumTitle)
                        },
                        onChangeSelected = { isSelected, subAlbumId ->
                            onEvent(AlbumDetailEvent.OnChangeSubAlbumSelected(isSelected, subAlbumId))
                        },
                        onBack = {
                            onEvent(if (uiState().isEditMode) AlbumDetailEvent.OnCancelEdit else AlbumDetailEvent.OnBack)
                        },
                    ),
                )

                }
            }

            composable<AlbumDetailTabDestination.Member> {
                ScreenContent(uiState().members, { onEvent(AlbumDetailEvent.OnRetryTab(DetailTab.Members)) }) { content ->
                MemberScreen(
                    memberList = { content.items },
                    isEditMode = { uiState().isEditMode },
                    getMembers = { onEvent(AlbumDetailEvent.OnMemberEvents(MemberEvent.GetMembers)) },
                    getFriends = { onEvent(AlbumDetailEvent.OnGetFriendList) },
                    getIsSelected = { uiState().selectedMemberIds.contains(it) },
                    onChangeMemberSelected = { isSelected, memberId ->
                        onEvent(
                            AlbumDetailEvent.OnChangeMemberSelected(
                                isSelected = isSelected,
                                memberId = memberId,
                            ),
                        )
                    },
                    onBack = {
                        if (uiState().isEditMode) {
                            onEvent(AlbumDetailEvent.OnCancelEdit)
                        } else {
                            onEvent(AlbumDetailEvent.OnBack)
                        }
                    },
                )

                }
            }

            composable<AlbumDetailTabDestination.AlbumSetting> {
                ScreenContent(uiState().settings, { onEvent(AlbumDetailEvent.OnRetryTab(DetailTab.Settings)) }) { content ->
                AlbumSettingRoute(
                    memberAuth = { content.permission },
                    onClickChangeImage = navActions.navigateToChangeImage,
                    onOpenTextFieldDialog = { onEvent(AlbumDetailEvent.OnOpenTextFieldDialog(it)) },
                    onDeleteAlbum = {
                        onEvent(AlbumDetailEvent.OnAlbumSettingEvents(AlbumSettingEvent.DeleteAlbum))
                    },
                    onExitAlbum = { onEvent(AlbumDetailEvent.OnMemberEvents(MemberEvent.Exit)) },
                    onBack = { onEvent(AlbumDetailEvent.OnBack) },
                )

                }
            }

            composable<AlbumDetailTabDestination.ChangeImage> {
                ScreenContent(uiState().settings, { onEvent(AlbumDetailEvent.OnRetryTab(DetailTab.Settings)) }) { content ->
                ChangeImageRoute(
                    coroutineScope = coroutineScope,
                    imageLoader = imageLoader,
                    imageUri = { uiState().imageUri },
                    title = { uiState().albumItem.title },
                    subTitle = { uiState().albumItem.subTitle },
                    onChangeIsInChangeImage = { onEvent(AlbumDetailEvent.OnChangeIsInChangeImage(it)) },
                    onSelectImage = { onEvent(AlbumDetailEvent.OnSelectImage(it)) },
                    onBack = {
                        onEvent(AlbumDetailEvent.OnBackInAlbumDetail)
                    },
                )

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlbumDetailTopAppBar(
    title: () -> String,
    subTitle: () -> String,
    selectedNavIndex: () -> Int,
    canInteract: () -> Boolean,
    canEdit: () -> Boolean,
    isEditMode: () -> Boolean,
    isInChangeImage: () -> Boolean,
    onClickBack: () -> Unit,
    onClickAdd: () -> Unit,
    onClickEdit: () -> Unit,
    onClickCancel: () -> Unit,
    onClickConfirm: () -> Unit,
    onClickChangeImage: () -> Unit,
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
                Icon(Icons.AutoMirrored.Default.ArrowBack, "뒤로")
            }
        },
        actions = {
            if (canInteract() && isEditMode().not()) {
                if (selectedNavIndex() != 2) {
                    IconButton(onClick = onClickAdd) {
                        Icon(Icons.Default.Add, "추가")
                    }
                    if (canEdit()) {
                        IconButton(onClick = onClickEdit) {
                            Icon(Icons.Default.Settings, "선택 편집")
                        }
                    }
                } else if (isInChangeImage()) {
                    TextButton(onClick = onClickChangeImage) {
                        Text(text = "적용", color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (canInteract()) {
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
internal fun AlbumDetailBottomNavigation(
    items: () -> List<BottomNavigationItem>,
    selectedNavIndex: () -> Int,
    onChangeNavIndex: (Int) -> Unit,
    onChangeIsInChangeImage: () -> Unit,
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
                        if (index != 2) {
                            onChangeIsInChangeImage()
                        }
                    }
                },
            )
        }
    }
}
