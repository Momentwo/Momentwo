package cord.eoeo.momentwo.feature.photo.impl.local
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.feature.photo.impl.photolist.*
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocalPhotoListRoute(loader: ImageLoader, back: () -> Unit, open: (String) -> Unit, vm: LocalPhotoListViewModel=hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle();val messages=remember{SnackbarHostState()}
    LaunchedEffect(vm){vm.initialize()}
    CollectEffect(vm.effect){when(val e=it){LocalPhotoListEffect.Back->back();is LocalPhotoListEffect.Open->open(e.id);is LocalPhotoListEffect.Message->launch{messages.showSnackbar(e.text)}}}
    BackHandler { vm.onEvent(LocalPhotoListEvent.Back) }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()){vm.onEvent(LocalPhotoListEvent.Import(it))}
    val content=(state.screen as? ScreenState.Success)?.content
    val album=content?.album
    val writable=album!=null && !album.copying && (album.libraryId=="device" || album.role!=null || (album.locallyCreated && album.remoteId==null))
    val manage=writable && (album?.libraryId=="device" || album?.role in setOf("ROLE_ALBUM_ADMIN","ROLE_ALBUM_SUB_ADMIN") || (album?.locallyCreated==true && album.remoteId==null))
    if(state.rename)LibraryEditDialog("서브 앨범 제목 변경",state.draft.text,pending="rename" in state.pending,onText={vm.onEvent(LocalPhotoListEvent.Text(it))},onSave={vm.onEvent(LocalPhotoListEvent.SaveName)},onDismiss={vm.onEvent(LocalPhotoListEvent.Dismiss)})
    if(state.confirmDelete)AlertDialog(onDismissRequest={vm.onEvent(LocalPhotoListEvent.Dismiss)},title={Text("선택 사진 삭제")},text={Text("선택한 사진과 기록을 삭제할까요? 가져온 원본 파일은 유지됩니다.")},confirmButton={TextButton(onClick={vm.onEvent(LocalPhotoListEvent.ConfirmDelete)},enabled="delete" !in state.pending){Text("삭제")}},dismissButton={TextButton(onClick={vm.onEvent(LocalPhotoListEvent.Dismiss)}){Text("취소")}})
    Scaffold(topBar={PhotoListTopAppBar({album?.title ?: state.header?.albumTitle.orEmpty()},{content?.subAlbum?.title ?: state.header?.subAlbumTitle.orEmpty()},{state.menu},{state.editing},
        {vm.onEvent(LocalPhotoListEvent.Back)},{picker.launch("image/*")},{vm.onEvent(LocalPhotoListEvent.Menu(true))},{vm.onEvent(LocalPhotoListEvent.Menu(false))},
        {vm.onEvent(LocalPhotoListEvent.Rename)},{vm.onEvent(LocalPhotoListEvent.Edit(true))},{vm.onEvent(LocalPhotoListEvent.Edit(false))},{vm.onEvent(LocalPhotoListEvent.Delete)},
        canAdd=writable && !state.importing,canRename=manage,canDelete=writable)},snackbarHost={SnackbarHost(messages)}) { padding ->
        PullToRefreshBox(isRefreshing=(state.screen as? ScreenState.Success)?.refresh==RefreshState.Loading,onRefresh={vm.onEvent(LocalPhotoListEvent.Retry)},modifier=Modifier.fillMaxSize().padding(padding).padding(horizontal=8.dp)) {
            ScreenContent(state.screen,{vm.onEvent(LocalPhotoListEvent.Retry)}) { ready ->
                if(ready.photos.isEmpty())EmptyContent()else LazyVerticalGrid(GridCells.Adaptive(100.dp),Modifier.fillMaxSize()) {
                    items(ready.photos,key={it.localId}) { photo -> PhotoGridTile(loader,photo.imageUri,if(photo.libraryId=="device")photo.favorite else photo.isLiked,
                        {state.editing},{photo.localId in state.selected},{vm.onEvent(LocalPhotoListEvent.Select(photo.localId,it))},{vm.onEvent(LocalPhotoListEvent.Open(photo.localId))},Modifier.animateItem()) }
                }
            }
        }
    }
}
