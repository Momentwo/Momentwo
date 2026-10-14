package cord.eoeo.momentwo.feature.photo.impl.local
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.ImageLoader
import coil.compose.AsyncImage
import cord.eoeo.momentwo.core.common.ScreenState
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.feature.photo.api.*
import kotlinx.coroutines.launch
fun NavGraphBuilder.localPhotoScreens(imageLoader: ImageLoader, controller: WindowInsetsControllerCompat, popBackStack: () -> Unit, openPhoto: (String) -> Unit) {
    composable<LocalPhotoListDestination> { LocalPhotoListRoute(imageLoader,popBackStack,openPhoto) }
    composable<LocalPhotoDetailDestination> { LocalPhotoRoute(imageLoader,controller,popBackStack) }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun LocalPhotoListRoute(loader: ImageLoader, back: () -> Unit, open: (String) -> Unit, vm: LocalPhotoListViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle(); val messages = remember { SnackbarHostState() }
    LaunchedEffect(vm) { vm.initialize() }
    CollectEffect(vm.effect) { when(val e=it) {LocalPhotoListEffect.Back->back();is LocalPhotoListEffect.Open->open(e.id);is LocalPhotoListEffect.Message->launch {messages.showSnackbar(e.text)}} }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {vm.onEvent(LocalPhotoListEvent.Import(it))}
    Scaffold(topBar={TopAppBar(title={Text((state.screen as? ScreenState.Success)?.content?.subAlbum?.title ?: "사진")},navigationIcon={TextButton(onClick={vm.onEvent(LocalPhotoListEvent.Back)}){Text("뒤로")}})},snackbarHost={SnackbarHost(messages)},floatingActionButton={if(state.screen is ScreenState.Success) FloatingActionButton(onClick={if(!state.importing) picker.launch("image/*")}){Text(if(state.importing) "가져오는 중" else "사진 추가")}}) {padding->
        ScreenContent(state.screen,{vm.onEvent(LocalPhotoListEvent.Retry)},Modifier.padding(padding)) {content->
            if(content.photos.isEmpty()) Text("사진을 추가하면 이 기기에 보관됩니다",Modifier.padding(24.dp))
            else LazyVerticalGrid(GridCells.Adaptive(120.dp),Modifier.fillMaxSize(),contentPadding=PaddingValues(8.dp)) {items(content.photos,key={it.localId}) {photo-> AsyncImage(photo.imageUri,"사진 열기",loader,modifier=Modifier.padding(4.dp).height(140.dp).clickable {vm.onEvent(LocalPhotoListEvent.Open(photo.localId))})}}
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun LocalPhotoRoute(loader: ImageLoader,controller: WindowInsetsControllerCompat,back:()->Unit,vm:LocalPhotoViewModel=hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle(); val messages=remember {SnackbarHostState()}
    LaunchedEffect(vm){vm.initialize()}; SystemBarsEffect(controller,!state.immersive)
    CollectEffect(vm.effect){when(val e=it){LocalPhotoEffect.Back->back();is LocalPhotoEffect.Message->launch {messages.showSnackbar(e.text)}}}
    when(val dialog=state.dialog){
        LocalPhotoDialog.None->Unit
        LocalPhotoDialog.Caption,is LocalPhotoDialog.Memo,is LocalPhotoDialog.Comment->LibraryEditDialog(title=if(dialog==LocalPhotoDialog.Caption) "사진 설명" else if(dialog is LocalPhotoDialog.Comment)"공유 댓글"else "개인 메모",text=state.draft.text,pending=if(dialog==LocalPhotoDialog.Caption) "caption" in state.pending else when(dialog){is LocalPhotoDialog.Memo->"memo:${dialog.id ?: "new"}" in state.pending;is LocalPhotoDialog.Comment->"comment:${dialog.id ?: "new"}" in state.pending;LocalPhotoDialog.None,LocalPhotoDialog.Caption,LocalPhotoDialog.Delete->false},onText={vm.onEvent(LocalPhotoEvent.Text(it))},onSave={vm.onEvent(LocalPhotoEvent.Save)},onDismiss={vm.onEvent(LocalPhotoEvent.Dismiss)},textLabel=if(dialog==LocalPhotoDialog.Caption) "설명" else if(dialog is LocalPhotoDialog.Comment)"댓글"else "메모",allowEmpty=dialog==LocalPhotoDialog.Caption)
        LocalPhotoDialog.Delete->AlertDialog(onDismissRequest={vm.onEvent(LocalPhotoEvent.Dismiss)},title={Text("사진 삭제")},text={Text(if((state.screen as? ScreenState.Success)?.content?.photo?.libraryId=="device")"설명·메모도 삭제됩니다. 가져온 원본 파일은 유지됩니다."else "계정 사진의 삭제를 기기에 저장하고 서버에 반영합니다. 별도 기기 원본은 유지됩니다.")},confirmButton={TextButton(onClick={vm.onEvent(LocalPhotoEvent.ConfirmDelete)},enabled="delete" !in state.pending){Text("삭제")}},dismissButton={TextButton(onClick={vm.onEvent(LocalPhotoEvent.Dismiss)}){Text("취소")}})
    }
    Scaffold(topBar={if(!state.immersive) TopAppBar(title={Text("사진과 기록")},navigationIcon={TextButton(onClick={vm.onEvent(LocalPhotoEvent.Back)}){Text("뒤로")}})},snackbarHost={SnackbarHost(messages)}) {padding->
        ScreenContent(state.screen,{vm.onEvent(LocalPhotoEvent.Retry)},Modifier.padding(padding)) {content->
            if(state.immersive) AsyncImage(content.photo.imageUri,"몰입 모드 닫기",loader,modifier=Modifier.fillMaxSize().clickable {vm.onEvent(LocalPhotoEvent.ToggleImmersive)})
            else LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                item { AsyncImage(content.photo.imageUri,"사진",loader,modifier=Modifier.fillMaxWidth().height(280.dp)); Text(if(content.photo.libraryId=="device")"이 기기에 저장됨"else "기기에 보관된 계정 자료 · 동기화 상태는 계정 메뉴에서 확인")
                    Row {TextButton(onClick={vm.onEvent(LocalPhotoEvent.ToggleImmersive)}){Text("몰입 모드")};TextButton(onClick={vm.onEvent(LocalPhotoEvent.Favorite)},enabled="favorite" !in state.pending){Text(if(content.photo.favorite) "즐겨찾기 해제" else "즐겨찾기")}}
                    Row {TextButton(onClick={vm.onEvent(LocalPhotoEvent.Export)},enabled="export" !in state.pending){Text("갤러리로 내보내기")};TextButton(onClick={vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Delete))},enabled=!content.photo.copying && (content.photo.libraryId=="device" || content.photo.role!=null || content.photo.remoteId==null)){Text("사진 삭제")}}
                    if(content.photo.libraryId!="device"){if(content.photo.likesKnown && content.photo.likeCount!=null){Text("좋아요 ${content.photo.likeCount}");TextButton(onClick={vm.onEvent(LocalPhotoEvent.Like)},enabled="like" !in state.pending && !content.photo.copying){Text(if(content.photo.isLiked)"좋아요 취소"else "좋아요")}}else Text("아직 저장된 좋아요 정보가 없습니다");TextButton(onClick={vm.onEvent(LocalPhotoEvent.Refresh)}){Text("계정 정보 새로고침")}}
                    Text(if(content.photo.descriptionKnown)content.photo.description.ifEmpty {"설명이 없습니다"}else "아직 저장된 설명이 없습니다");TextButton(onClick={vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Caption))},enabled=!content.photo.copying && (content.photo.libraryId=="device" || content.photo.role!=null || content.photo.remoteId==null)){Text("설명 편집")}
                    if(content.photo.libraryId!="device") {
                        TextButton(onClick={vm.onEvent(LocalPhotoEvent.OpenComments)}){Text("공유 댓글 보기")}
                        if(state.commentsOpen) {
                            ScreenContent(content.comments,{vm.onEvent(LocalPhotoEvent.OpenComments)}) { comments ->
                                Column {
                                    comments.forEach { comment ->
                                        Text("${comment.author} · ${comment.text}")
                                        if(comment.mine) Row {
                                            TextButton(onClick={vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Comment(comment.localId)))},enabled=!content.photo.copying){Text("댓글 수정")}
                                            TextButton(onClick={vm.onEvent(LocalPhotoEvent.DeleteComment(comment.localId))},enabled=!content.photo.copying){Text("댓글 삭제")}
                                        }
                                    }
                                }
                            }
                            TextButton(onClick={vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Comment(null)))},enabled=!content.photo.copying){Text("공유 댓글 작성")}
                        }
                    }

                    Text("개인 메모 · 나");TextButton(onClick={vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Memo(null)))}){Text("메모 추가")}
                }
                items(content.memos,key={it.localId}) {memo-> ListItem(headlineContent={Text(memo.text)},supportingContent={Text("나")},trailingContent={Row {TextButton(onClick={vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Memo(memo.localId)))}){Text("수정")};TextButton(onClick={vm.onEvent(LocalPhotoEvent.DeleteMemo(memo.localId))},enabled="delete-memo:${memo.localId}" !in state.pending){Text("메모 삭제")}}})}
            }
        }
    }
}
