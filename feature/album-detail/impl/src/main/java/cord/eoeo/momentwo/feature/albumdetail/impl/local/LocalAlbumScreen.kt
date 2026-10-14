package cord.eoeo.momentwo.feature.albumdetail.impl.local

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.ImageLoader
import coil.compose.AsyncImage
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.feature.albumdetail.api.LocalAlbumDetailDestination
import kotlinx.coroutines.launch

fun NavGraphBuilder.localAlbumDetailScreen(imageLoader: ImageLoader, popBackStack: () -> Unit, navigateToLogin: () -> Unit, openPhotos: (String) -> Unit, openAccountPhotos: (Int,Int,String,String) -> Unit = {_,_,_,_->}, openSharedAlbum: (Int,String,String,String) -> Unit = {_,_,_,_->}) {
    composable<LocalAlbumDetailDestination> { LocalAlbumRoute(imageLoader, popBackStack, navigateToLogin, openPhotos, openAccountPhotos, openSharedAlbum) }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalAlbumRoute(imageLoader: ImageLoader, popBackStack: () -> Unit, navigateToLogin: () -> Unit, openPhotos: (String) -> Unit, openAccountPhotos: (Int,Int,String,String) -> Unit, openSharedAlbum: (Int,String,String,String) -> Unit, vm: LocalAlbumViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val messages = remember { SnackbarHostState() }
    LaunchedEffect(vm) { vm.initialize() }
    CollectEffect(vm.effect) { when (val effect = it) { LocalAlbumEffect.Back -> popBackStack(); LocalAlbumEffect.Connect -> navigateToLogin(); is LocalAlbumEffect.Message -> launch { messages.showSnackbar(effect.text) } } }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { uri -> vm.onEvent(LocalAlbumEvent.Cover(uri)) } }
    when (val dialog = state.dialog) {
        LocalAlbumDialog.None -> Unit
        LocalAlbumDialog.CreateSub, LocalAlbumDialog.EditAlbum, is LocalAlbumDialog.RenameSub -> LibraryEditDialog(
            title = when (dialog) { LocalAlbumDialog.CreateSub -> "서브앨범 만들기"; LocalAlbumDialog.EditAlbum -> "앨범 편집"; is LocalAlbumDialog.RenameSub -> "서브앨범 이름 변경"; LocalAlbumDialog.None, LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> error("Not an editor") },
            text = state.draft.text, extra = if (dialog == LocalAlbumDialog.EditAlbum) state.draft.extra else null,
            pending = when (dialog) { LocalAlbumDialog.CreateSub -> "create-sub" in state.pending; LocalAlbumDialog.EditAlbum -> "edit-album" in state.pending; is LocalAlbumDialog.RenameSub -> "rename:${dialog.id}" in state.pending; LocalAlbumDialog.None, LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> false }, onText = { vm.onEvent(LocalAlbumEvent.Text(it)) }, onExtra = { vm.onEvent(LocalAlbumEvent.Extra(it)) }, onSave = { vm.onEvent(LocalAlbumEvent.Save) }, onDismiss = { vm.onEvent(LocalAlbumEvent.Dismiss) })
        LocalAlbumDialog.DeleteAlbum, is LocalAlbumDialog.DeleteSub -> AlertDialog(onDismissRequest = { vm.onEvent(LocalAlbumEvent.Dismiss) }, title = { Text("앨범 삭제") }, text = { Text(if((state.screen as? ScreenState.Success)?.content?.album?.libraryId=="device")"앨범과 포함된 사진·기록을 삭제할까요? 가져온 원본 사진은 변경하지 않습니다."else "계정 앨범의 삭제를 기기에 저장하고 서버에 반영합니다. 별도 기기 원본은 유지됩니다.") }, confirmButton = { TextButton(onClick = { vm.onEvent(LocalAlbumEvent.ConfirmDelete) }, enabled = state.pending.isEmpty()) { Text("삭제") } }, dismissButton = { TextButton(onClick = { vm.onEvent(LocalAlbumEvent.Dismiss) }) { Text("취소") } })
    }
    val content = (state.screen as? ScreenState.Success)?.content
    Scaffold(topBar = { TopAppBar(title = { Text(content?.album?.title ?: "로컬 앨범") }, navigationIcon = { TextButton(onClick = { vm.onEvent(LocalAlbumEvent.Back) }) { Text("뒤로") } }) }, snackbarHost = { SnackbarHost(messages) }) { padding ->
        ScreenContent(state.screen, { vm.onEvent(LocalAlbumEvent.Retry) }, Modifier.padding(padding)) { ready ->
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                item {
                    if (ready.album.coverUri.isNotEmpty()) AsyncImage(ready.album.coverUri, "대표 사진", imageLoader, modifier = Modifier.fillMaxWidth().height(160.dp))
                    Text(ready.album.subtitle)
                    Text(if(ready.album.libraryId == "device") "이 기기에 저장됨" else if(ready.album.copying || ready.album.remoteId==null) "기기에 보관됨 · 계정 복사·동기화 대기" else "계정 앨범")
                    if(!ready.album.copying && (ready.album.libraryId=="device" || ready.album.role in setOf("ROLE_ALBUM_ADMIN","ROLE_ALBUM_SUB_ADMIN") || (ready.album.locallyCreated && ready.album.remoteId==null))) {
                    Row { TextButton(onClick = { vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.EditAlbum)) }) { Text("앨범 편집") }; TextButton(onClick = { picker.launch("image/*") }) { Text("대표 사진 변경") } }
                    Row { TextButton(onClick = { vm.onEvent(LocalAlbumEvent.Cover(null)) }) { Text("대표 사진 초기화") }; TextButton(onClick = { vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.DeleteAlbum)) },enabled=ready.album.libraryId=="device" || ready.album.role=="ROLE_ALBUM_ADMIN" || (ready.album.locallyCreated && ready.album.remoteId==null)) { Text("앨범 삭제") } }
                    TextButton(onClick = { vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.CreateSub)) }) { Text("서브앨범 추가") }
                    }
                }
                items(ready.subAlbums, key = { it.localId }) { sub ->
                    ListItem(modifier = Modifier.clickable { openPhotos(sub.localId) }, headlineContent = { Text(sub.title) }, trailingContent = { if(!ready.album.copying && (ready.album.libraryId=="device" || ready.album.role in setOf("ROLE_ALBUM_ADMIN","ROLE_ALBUM_SUB_ADMIN") || (ready.album.locallyCreated && ready.album.remoteId==null))) Row {
                        TextButton(onClick = { vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.RenameSub(sub.localId))) }) { Text("이름 변경") }
                        TextButton(onClick = { vm.onEvent(LocalAlbumEvent.Open(LocalAlbumDialog.DeleteSub(sub.localId))) }) { Text("삭제") }
                    } })
                }
                item { if(ready.album.libraryId != "device") ready.album.remoteId?.let { id -> TextButton(onClick={openSharedAlbum(id,ready.album.title,ready.album.subtitle,ready.album.coverUri)}){Text("멤버·공유 관리")} }; TextButton(onClick = { vm.onEvent(LocalAlbumEvent.Connect) }) { Text("계정 연결 후 공유") } }
            }
        }
    }
}
