package cord.eoeo.momentwo.feature.albumdetail.impl.local

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil3.ImageLoader
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.feature.albumdetail.api.LocalAlbumDetailDestination
import cord.eoeo.momentwo.feature.albumdetail.impl.AlbumDetailTopAppBar
import cord.eoeo.momentwo.feature.albumdetail.impl.AlbumDetailBottomNavigation
import cord.eoeo.momentwo.feature.albumdetail.impl.albumsetting.AlbumSettingsContent
import cord.eoeo.momentwo.feature.albumdetail.impl.albumsetting.ChangeImageScreen
import cord.eoeo.momentwo.feature.albumdetail.impl.member.MemberScreen
import kotlinx.coroutines.launch

fun NavGraphBuilder.localAlbumDetailScreen(imageLoader: ImageLoader, popBackStack: () -> Unit, navigateToLogin: () -> Unit, openPhotos: (String) -> Unit, openAccountPhotos: (Int,Int,String,String) -> Unit = {_,_,_,_->}, openSharedAlbum: (Int,String,String,String) -> Unit = {_,_,_,_->}) {
    composable<LocalAlbumDetailDestination> { LocalAlbumRoute(imageLoader, popBackStack, navigateToLogin, openPhotos) }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalAlbumRoute(loader: ImageLoader, back: () -> Unit, connect: () -> Unit, openPhotos: (String) -> Unit, vm: LocalAlbumViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val messages = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val sheet = rememberModalBottomSheetState()
    var coverMenu by remember { mutableStateOf(false) }
    var guide by remember { mutableStateOf(false) }
    LaunchedEffect(vm) { vm.initialize() }
    CollectEffect(vm.effect) { when (val e = it) { LocalAlbumEffect.Back -> back(); LocalAlbumEffect.Connect -> guide = true; is LocalAlbumEffect.Message -> launch { messages.showSnackbar(e.text) } } }
    BackHandler { vm.onEvent(LocalAlbumEvent.Back) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { uri -> vm.onEvent(LocalAlbumEvent.Cover(uri)) } }
    when (val dialog = state.dialog) {
        LocalAlbumDialog.None -> Unit
        LocalAlbumDialog.CreateSub, LocalAlbumDialog.EditTitle, LocalAlbumDialog.EditSubtitle, LocalAlbumDialog.EditAlbum, is LocalAlbumDialog.RenameSub -> LibraryEditDialog(
            title = when(dialog) { LocalAlbumDialog.CreateSub -> "서브앨범 생성";LocalAlbumDialog.EditTitle -> "제목 변경";LocalAlbumDialog.EditSubtitle -> "부제목 변경";LocalAlbumDialog.EditAlbum -> "앨범 편집";is LocalAlbumDialog.RenameSub -> "서브앨범 이름 변경";LocalAlbumDialog.None, LocalAlbumDialog.Invite, LocalAlbumDialog.Leave, LocalAlbumDialog.DeleteAlbum, LocalAlbumDialog.DeleteSelected, is LocalAlbumDialog.DeleteSub -> error("editor") },
            text=state.draft.text,extra=if(dialog==LocalAlbumDialog.EditAlbum)state.draft.extra else null,pending=state.pending.isNotEmpty(),
            onText={vm.onEvent(LocalAlbumEvent.Text(it))},onExtra={vm.onEvent(LocalAlbumEvent.Extra(it))},onSave={vm.onEvent(LocalAlbumEvent.Save)},onDismiss={vm.onEvent(LocalAlbumEvent.Dismiss)},allowEmpty=dialog==LocalAlbumDialog.EditSubtitle)
        LocalAlbumDialog.Leave, LocalAlbumDialog.DeleteAlbum, LocalAlbumDialog.DeleteSelected, is LocalAlbumDialog.DeleteSub -> AlertDialog(
            onDismissRequest={vm.onEvent(LocalAlbumEvent.Dismiss)},title={Text(if(dialog==LocalAlbumDialog.Leave)"앨범 나가기"else if(dialog==LocalAlbumDialog.DeleteSelected)"선택 항목 삭제"else "앨범 삭제")},
            text={Text(if(dialog==LocalAlbumDialog.Leave)"이 계정 앨범에서 나갈까요? 기기 원본은 유지됩니다."else if(state.tab==1)"선택한 멤버를 추방할까요?"else "선택한 앨범과 사진·기록을 삭제할까요? 가져온 원본 파일은 변경하지 않습니다.")},
            confirmButton={TextButton(onClick={vm.onEvent(LocalAlbumEvent.ConfirmDelete)},enabled=state.pending.isEmpty()){Text(if(dialog==LocalAlbumDialog.Leave)"나가기"else "삭제")}},dismissButton={TextButton(onClick={vm.onEvent(LocalAlbumEvent.Dismiss)}){Text("취소")}})
        LocalAlbumDialog.Invite -> when(val friends=state.friends) {
            ScreenState.Loading,is ScreenState.Error -> androidx.compose.ui.window.Dialog(onDismissRequest={vm.onEvent(LocalAlbumEvent.Dismiss)}){ScreenContent(friends,{vm.onEvent(LocalAlbumEvent.Add)}){}}
            is ScreenState.Success -> InviteDialog({friends.content},{state.invited},{friend->state.invited.any{it.nickname==friend.nickname}},{checked,friend->vm.onEvent(LocalAlbumEvent.InviteCheck(friend,checked))},{vm.onEvent(LocalAlbumEvent.Dismiss)},{vm.onEvent(LocalAlbumEvent.ConfirmInvite)})
        }
    }
    val album=(state.screen as? ScreenState.Success)?.content?.album
    val owned=album?.libraryId=="device" || (album?.locallyCreated==true && album.remoteId==null)
    val manage=album!=null && !album.copying && (owned || album.role in setOf("ROLE_ALBUM_ADMIN","ROLE_ALBUM_SUB_ADMIN"))
    val delete=album!=null && !album.copying && (owned || album.role=="ROLE_ALBUM_ADMIN")
    if(guide) AlertDialog(onDismissRequest={guide=false},title={Text("친구와 앨범 공유")},text={Text("기기 앨범의 멤버와 초대는 계정 연결 후 선택한 앨범을 복사하여 사용할 수 있습니다. 기기 원본은 유지됩니다.")},confirmButton={TextButton(onClick={guide=false;connect()}){Text("계정 연결·복사")}},dismissButton={TextButton(onClick={guide=false}){Text("닫기")}})
    val settingsColor=MaterialTheme.colorScheme.onSurfaceVariant
    val navItems=listOf(BottomNavigationItem("서브 앨범",Icons.Filled.PhotoAlbum,Icons.Outlined.PhotoAlbum,{}),BottomNavigationItem("멤버",Icons.Filled.People,Icons.Outlined.People,{}),BottomNavigationItem("설정",Icons.Filled.Settings,Icons.Outlined.Settings,{}))
    Scaffold(topBar={AlbumDetailTopAppBar({album?.title.orEmpty()},{album?.subtitle.orEmpty()},{state.tab},
        {album!=null && (state.tab==1 || manage)},{manage && (state.tab==0 || !owned)}, {state.editing},{state.coverPage},
        {vm.onEvent(LocalAlbumEvent.Back)},{vm.onEvent(LocalAlbumEvent.Add)},{vm.onEvent(LocalAlbumEvent.Edit)},
        {vm.onEvent(LocalAlbumEvent.CancelEdit)},{vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.DeleteSelected))},{vm.onEvent(LocalAlbumEvent.ApplyCover)},scroll)},
        bottomBar={AlbumDetailBottomNavigation({navItems},{state.tab},{vm.onEvent(LocalAlbumEvent.Tab(it))},{})},snackbarHost={SnackbarHost(messages)},modifier=Modifier.nestedScroll(scroll.nestedScrollConnection)) { padding ->
        ScreenContent(state.screen,{vm.onEvent(LocalAlbumEvent.Retry)},Modifier.padding(padding)) { content ->
            when(state.tab) {
                0 -> if(!content.album.subsLoaded) ScreenContent<Any>((state.screen as? ScreenState.Success)?.refresh.let { if(it is cord.eoeo.momentwo.core.common.RefreshState.Error)ScreenState.Error(it.error)else ScreenState.Loading }, {vm.onEvent(LocalAlbumEvent.Retry)}) {} else if(content.subAlbums.isEmpty()) EmptyContent() else LazyVerticalGrid(GridCells.Adaptive(160.dp),Modifier.fillMaxSize()) {
                    items(content.subAlbums,key={it.localId}) { sub ->
                    val colors=if(sub.localId in state.selected && state.editing)CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.tertiaryContainer)else CardDefaults.cardColors()
                    SubAlbumPreviewCard(loader,sub.title,sub.previews.map{SubAlbumPreview(it.localId,it.imageUri)},
                        {sub.localId in state.selected},{colors},
                        {state.editing},{openPhotos(sub.localId)},{vm.onEvent(LocalAlbumEvent.Select(sub.localId,it))},Modifier.animateItem()) }
                }
                1 -> if(owned || content.album.copying || content.album.remoteId==null) Column(Modifier.padding(24.dp)) {
                    Text("친구와 앨범을 공유하려면 계정을 연결하고 선택한 앨범을 복사하세요.")
                    TextButton(onClick={guide=true}) { Text("계정 연결·복사") }
                } else ScreenContent(state.members,{vm.onEvent(LocalAlbumEvent.Retry)}) { members -> MemberScreen({members},{state.editing},{},{},{it in state.selectedMembers},{checked,id->vm.onEvent(LocalAlbumEvent.SelectMember(id,checked))},{vm.onEvent(LocalAlbumEvent.Back)}) }
                2 -> if(state.coverPage) ChangeImageScreen(scope,loader,{(if(state.coverDirty)state.coverDraft else content.album.coverUri)?.takeIf{it.isNotBlank()}?.let(android.net.Uri::parse)},
                    {content.album.title},{content.album.subtitle},{coverMenu},{sheet},{},{coverMenu=it},{picker.launch("image/*")},{vm.onEvent(LocalAlbumEvent.Cover(null))},{vm.onEvent(LocalAlbumEvent.Back)})
                else AlbumSettingsContent(rememberScrollableState{0f},manage,delete,{settingsColor},
                    {vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.EditTitle))},{vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.EditSubtitle))},{vm.onEvent(LocalAlbumEvent.OpenCover)},{vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.DeleteAlbum))},if(owned || content.album.remoteId==null)null else ({vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.Leave))}))
            }
        }
    }
}
