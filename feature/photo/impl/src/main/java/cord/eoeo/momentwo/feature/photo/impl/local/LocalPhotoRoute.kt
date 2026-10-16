package cord.eoeo.momentwo.feature.photo.impl.local
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.PhotoDescription
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.core.designsystem.theme.backgroundDark
import cord.eoeo.momentwo.feature.photo.impl.presentation.*
import cord.eoeo.momentwo.feature.photo.impl.photodetail.PhotoDetailBottomBar
import cord.eoeo.momentwo.feature.photo.impl.photodetail.composable.DescriptionBottomSheetScreen
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
@OptIn(ExperimentalMaterial3Api::class,ExperimentalLayoutApi::class)
@Composable
internal fun LocalPhotoRoute(loader: ImageLoader, controller: WindowInsetsControllerCompat, back: () -> Unit, vm: LocalPhotoViewModel=hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle();val messages=remember{SnackbarHostState()}
    var menu by remember{mutableStateOf(false)}
    val visible=!state.immersive || state.sheet!=LocalPhotoSheet.None
    val background by animateColorAsState(if(visible)MaterialTheme.colorScheme.background else backgroundDark,spring(stiffness=Spring.StiffnessMediumLow),label="backgroundColor")
    LaunchedEffect(vm){vm.initialize()};SystemBarsEffect(controller,visible)
    CollectEffect(vm.effect){when(val e=it){LocalPhotoEffect.Back->back();is LocalPhotoEffect.Message->launch{messages.showSnackbar(e.text)}}}
    val content=(state.screen as? ScreenState.Success)?.content
    val photo=content?.photo
    val device=photo?.libraryId=="device"
    val writable=photo!=null && !photo.copying && (device || photo.role!=null || photo.remoteId==null)
    val description=PhotoDescription.read(photo?.description.orEmpty())
    val editing=state.dialog==LocalPhotoDialog.Caption
    if(state.sheet==LocalPhotoSheet.Description && photo!=null) {
        val sheet=rememberModalBottomSheetState(skipPartiallyExpanded=true);val scroll=rememberScrollState();val nested=rememberNestedScrollInteropConnection();val ime=WindowInsets.isImeVisible
        var titleInput by remember { mutableStateOf(TextFieldValue(state.draft.text)) }
        var bodyInput by remember { mutableStateOf(TextFieldValue(state.draft.extra)) }
        LaunchedEffect(state.draft.text,editing) { if(editing && titleInput.text!=state.draft.text)titleInput=TextFieldValue(state.draft.text,TextRange(state.draft.text.length)) }
        LaunchedEffect(state.draft.extra,editing) { if(editing && bodyInput.text!=state.draft.extra)bodyInput=TextFieldValue(state.draft.extra,TextRange(state.draft.extra.length)) }
        var maxLines by remember{mutableIntStateOf(4)}
        DescriptionBottomSheetScreen(loader,{sheet},{scroll},{nested},{ime},{editing && writable},{description.body.lines().size>4},
            {if(editing)titleInput else TextFieldValue(description.title)},{if(editing)bodyInput else TextFieldValue(description.body)},{maxLines},
            {""},{""},{""},{titleInput=it;vm.onEvent(LocalPhotoEvent.Text(it.text))},{bodyInput=it;vm.onEvent(LocalPhotoEvent.Body(it.text))},{maxLines=Int.MAX_VALUE},
            {vm.onEvent(LocalPhotoEvent.Sheet(LocalPhotoSheet.None))},{vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Caption))},{vm.onEvent(LocalPhotoEvent.Dismiss)},
            {vm.onEvent(LocalPhotoEvent.Save)},{vm.onEvent(LocalPhotoEvent.Save)},{vm.onEvent(LocalPhotoEvent.DeleteDescription)},
            refresh=(state.screen as? ScreenState.Success)?.refresh ?: RefreshState.Idle,onRetry={vm.onEvent(LocalPhotoEvent.Refresh)},
            hasDescription={photo.descriptionKnown && photo.description.isNotEmpty()},canEdit=writable,pending="caption" in state.pending)
    }
    if((state.sheet==LocalPhotoSheet.Memos || state.sheet==LocalPhotoSheet.Comments) && content!=null) {
        val memo=state.sheet==LocalPhotoSheet.Memos
        val text=if((memo && state.dialog is LocalPhotoDialog.Memo) || (!memo && state.dialog is LocalPhotoDialog.Comment))state.draft.text else ""
        var input by remember{mutableStateOf(TextFieldValue(text))}
        LaunchedEffect(text){if(input.text!=text)input=TextFieldValue(text)}
        RecordsSheet(if(memo)"개인 메모"else "댓글",input,{value->input=value
            if(memo && state.dialog !is LocalPhotoDialog.Memo)vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Memo(null)))
            if(!memo && state.dialog !is LocalPhotoDialog.Comment)vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Comment(null)))
            vm.onEvent(LocalPhotoEvent.Text(value.text))
        },{vm.onEvent(LocalPhotoEvent.Save)},{vm.onEvent(LocalPhotoEvent.Sheet(LocalPhotoSheet.None))},
            pending=state.pending.any{it.startsWith(if(memo)"memo:"else "comment:")},canWrite=memo || writable) {
            if(memo)LazyColumn(Modifier.fillMaxSize()) { items(content.memos,key={it.localId}) { note -> RecordRow(loader,"나",DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(note.createdAt)),"",note.text,"메모",true,
                {vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Memo(note.localId)))},{vm.onEvent(LocalPhotoEvent.DeleteMemo(note.localId))},Modifier.animateItem()) };if(content.memos.isEmpty())item{EmptyContent()} }
            else ScreenContent(content.comments,{vm.onEvent(LocalPhotoEvent.OpenComments)}) { rows -> LazyColumn(Modifier.fillMaxSize()) {items(rows,key={it.localId}){comment->RecordRow(loader,comment.author,comment.date,"",comment.text,"댓글",comment.mine && writable,
                {vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Comment(comment.localId)))},{vm.onEvent(LocalPhotoEvent.DeleteComment(comment.localId))},Modifier.animateItem())};if(rows.isEmpty())item{EmptyContent()}} }
        }
    }
    if(state.dialog==LocalPhotoDialog.Delete)AlertDialog(onDismissRequest={vm.onEvent(LocalPhotoEvent.Dismiss)},title={Text("사진 삭제")},text={Text("사진과 설명·기록을 삭제할까요? 가져온 원본 파일은 유지됩니다.")},confirmButton={TextButton(onClick={vm.onEvent(LocalPhotoEvent.ConfirmDelete)},enabled="delete" !in state.pending){Text("삭제")}},dismissButton={TextButton(onClick={vm.onEvent(LocalPhotoEvent.Dismiss)}){Text("취소")}})
    val viewerState=if(state.sheet==LocalPhotoSheet.Description && state.screen is ScreenState.Success)(state.screen as ScreenState.Success).copy(refresh=RefreshState.Idle)else state.screen
    PhotoViewer(loader,photo?.imageUri,viewerState,background,visible,visible,messages,{vm.onEvent(LocalPhotoEvent.Back)},{vm.onEvent(LocalPhotoEvent.ToggleImmersive)},{vm.onEvent(LocalPhotoEvent.Retry)},
        actions={if(photo!=null){IconButton(onClick={menu=true}){Icon(Icons.Default.MoreVert,"더보기")};DropdownMenu(menu,{menu=false}){
            if(!device){DropdownMenuItem(text={Text(if(photo.favorite)"개인 즐겨찾기 해제"else "개인 즐겨찾기")},onClick={menu=false;vm.onEvent(LocalPhotoEvent.Favorite)},enabled="favorite" !in state.pending)
                DropdownMenuItem(text={Text("개인 메모")},onClick={menu=false;vm.onEvent(LocalPhotoEvent.Sheet(LocalPhotoSheet.Memos))})
                DropdownMenuItem(text={Text("계정 정보 새로고침")},onClick={menu=false;vm.onEvent(LocalPhotoEvent.Refresh)})}
            DropdownMenuItem(text={Text("사진 삭제")},onClick={menu=false;vm.onEvent(LocalPhotoEvent.Open(LocalPhotoDialog.Delete))},enabled=writable)
        }}}) {
        if(photo!=null)PhotoDetailBottomBar({if(device)null else photo.likeCount},{if(device)photo.favorite else photo.isLiked},
            {vm.onEvent(LocalPhotoEvent.Sheet(LocalPhotoSheet.Description))},{vm.onEvent(if(device)LocalPhotoEvent.Favorite else LocalPhotoEvent.Like)},
            {vm.onEvent(if(device)LocalPhotoEvent.Sheet(LocalPhotoSheet.Memos)else LocalPhotoEvent.OpenComments)},{vm.onEvent(LocalPhotoEvent.Export)},
            likeLabel=if(device)"즐겨찾기"else "좋아요",recordLabel=if(device)"개인 메모"else "사진 댓글",exportLabel="갤러리로 내보내기",
            canLike=if(device)"favorite" !in state.pending else writable && photo.likesKnown && "like" !in state.pending,
            canExport="export" !in state.pending)
    }
}
