package cord.eoeo.momentwo.feature.album.impl.sync
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
import cord.eoeo.momentwo.core.model.*
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.feature.album.api.AccountSyncDestination
import kotlinx.coroutines.launch
fun NavGraphBuilder.accountSyncScreen(loader:ImageLoader,back:()->Unit,open:(String)->Unit){composable<AccountSyncDestination>{AccountSyncRoute(loader,back,open)}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AccountSyncRoute(loader:ImageLoader,back:()->Unit,open:(String)->Unit,vm:AccountSyncViewModel=hiltViewModel()){
    val state by vm.uiState.collectAsStateWithLifecycle();val messages=remember{SnackbarHostState()};LaunchedEffect(vm){vm.initialize()};CollectEffect(vm.effect){when(val e=it){AccountSyncEffect.Back->back();is AccountSyncEffect.Album->open(e.id);is AccountSyncEffect.Message->launch{messages.showSnackbar(e.text)}}}
    val content=(state.screen as? ScreenState.Success)?.content
    fun button(text:String,event:AccountSyncEvent):@Composable ()->Unit={TextButton(onClick={vm.onEvent(event)},enabled=state.pending.isEmpty()){Text(text)}}
    when(val dialog=state.dialog){
        SyncDialog.None->Unit
        SyncDialog.Copy->AlertDialog(onDismissRequest={vm.onEvent(AccountSyncEvent.Dismiss)},title={Text("선택 앨범을 계정에 복사")},text={Text("대상 계정: ${content?.identity?.accountId}\n앨범 ${state.selected.size}개와 서브앨범·사진·설명을 시작 시점 기준으로 복사합니다. 기기 원본과 개인 메모·즐겨찾기는 유지하며 공유 댓글·좋아요로 변환하지 않습니다.")},confirmButton=button("복사 시작",AccountSyncEvent.ConfirmCopy),dismissButton=button("취소",AccountSyncEvent.Dismiss))
        SyncDialog.Create->LibraryEditDialog("계정 앨범 만들기",state.draft,pending="create" in state.pending,onText={vm.onEvent(AccountSyncEvent.Text(it))},onSave={vm.onEvent(AccountSyncEvent.Create)},onDismiss={vm.onEvent(AccountSyncEvent.Dismiss)})
        is SyncDialog.Link->AlertDialog(onDismissRequest={vm.onEvent(AccountSyncEvent.Dismiss)},title={Text("생성된 서버 항목 확인")},text={Column{Text("계정: ${content?.identity?.accountId}\n${dialog.job.displayName}\n생성 응답에 ID가 없어 자동으로 연결하지 않습니다. 실제 생성된 항목을 선택해주세요.");ScreenContent(dialog.candidates,{vm.onEvent(AccountSyncEvent.Link(dialog.job))}){rows->LazyColumn(Modifier.heightIn(max=320.dp)){items(rows,key={it.id}){candidate->TextButton(onClick={vm.onEvent(AccountSyncEvent.Bind(candidate.id))},enabled=state.pending.isEmpty()){if(candidate.imageUri.isNotBlank())AsyncImage(candidate.imageUri,null,loader,modifier=Modifier.size(48.dp));Text(candidate.title)}};if(rows.isEmpty())item{Text("아직 연결 가능한 항목이 없습니다")}}}}},confirmButton=button("닫기",AccountSyncEvent.Dismiss),dismissButton={if(dialog.job.status==SyncStatus.UNKNOWN && dialog.candidates is ScreenState.Success)button("확인 후 다시 전송",AccountSyncEvent.OpenRetry)()})
        is SyncDialog.Inspect->AlertDialog(onDismissRequest={vm.onEvent(AccountSyncEvent.Dismiss)},title={Text("서버 값과 내 변경 확인")},text={Column{Text(dialog.job.displayName);Text("내 변경: ${valueLabel(dialog.job.kind,dialog.job.desired)}");ScreenContent(dialog.value,{vm.onEvent(AccountSyncEvent.Inspect(dialog.job))}){Text("서버: ${valueLabel(dialog.job.kind,it)}")}}},confirmButton={if(dialog.value is ScreenState.Success)button("서버 값 사용",AccountSyncEvent.Server)()},dismissButton={Column{if(dialog.value is ScreenState.Success){if(dialog.job.status==SyncStatus.CONFLICT)button("내 변경 사용",AccountSyncEvent.Mine)()else if(dialog.job.status==SyncStatus.UNKNOWN)button("확인 후 다시 전송",AccountSyncEvent.OpenRetry)()};button("닫기",AccountSyncEvent.Dismiss)()}})
        is SyncDialog.Retry->AlertDialog(onDismissRequest={vm.onEvent(AccountSyncEvent.Dismiss)},title={Text("다시 전송할까요?")},text={Text("서버에 이미 반영되었을 수 있습니다. 목록과 상태를 확인했으며 재전송 시 중복 항목이 생길 수 있음을 확인해주세요. 기기 원본은 유지됩니다.")},confirmButton=button("확인하고 다시 전송",AccountSyncEvent.RetryConfirmed),dismissButton=button("취소",AccountSyncEvent.Dismiss))
        is SyncDialog.Cancel->AlertDialog(onDismissRequest={vm.onEvent(AccountSyncEvent.Dismiss)},title={Text("대기 작업 중단")},text={Text("${dialog.job.displayName}\n복사 작업은 기기 복사본과 남은 전송을 중단합니다. 원본은 유지하며 이미 서버에 반영된 자료를 자동 삭제하지 않습니다. 반영 여부가 불명확하면 먼저 서버 항목을 확인해주세요.")},confirmButton=button("중단",AccountSyncEvent.Cancel),dismissButton=button("돌아가기",AccountSyncEvent.Dismiss))
    }
    Scaffold(topBar={TopAppBar(title={Text("계정 복사·동기화")},navigationIcon={TextButton(onClick={vm.onEvent(AccountSyncEvent.Back)}){Text("뒤로")}})},snackbarHost={SnackbarHost(messages)}){padding->ScreenContent(state.screen,{vm.onEvent(AccountSyncEvent.Retry)},Modifier.padding(padding)){ready->LazyColumn(Modifier.fillMaxSize().padding(16.dp)){
        item{Text("대상 계정 · ${ready.identity.accountId}");Text("로그인만으로 기기 자료를 업로드하지 않습니다");TextButton(onClick={vm.onEvent(AccountSyncEvent.OpenCreate)}){Text("계정 앨범 만들기")};Text("복사할 기기 앨범 선택")}
        items(ready.albums,key={it.localId}){album->Row{Checkbox(checked=album.localId in state.selected,onCheckedChange={vm.onEvent(AccountSyncEvent.Toggle(album.localId))});Text(album.title,Modifier.padding(top=12.dp))}}
        item{TextButton(onClick={vm.onEvent(AccountSyncEvent.OpenCopy)},enabled=state.selected.isNotEmpty() && "copy" !in state.pending){Text("선택 앨범 복사")};TextButton(onClick={vm.onEvent(AccountSyncEvent.Retry)}){Text("대기 작업 이어가기")};Text("저장된 계정 작업")}
        items(ready.jobs,key={it.id}){job->Column(Modifier.padding(vertical=8.dp)){Text(job.displayName);Text(statusLabel(job.status));if(job.status in setOf(SyncStatus.AWAITING_ID,SyncStatus.UNKNOWN) && job.kind in creations)TextButton(onClick={vm.onEvent(AccountSyncEvent.Link(job))}){Text("서버 항목 확인")};if(job.status in setOf(SyncStatus.UNKNOWN,SyncStatus.CONFLICT,SyncStatus.PERMISSION_DENIED,SyncStatus.NOT_FOUND) && job.kind !in creations)TextButton(onClick={vm.onEvent(AccountSyncEvent.Inspect(job))}){Text("서버 값 확인")};if(job.status in setOf(SyncStatus.QUEUED,SyncStatus.PREPARING,SyncStatus.AUTH_REQUIRED,SyncStatus.PERMISSION_DENIED,SyncStatus.NOT_FOUND,SyncStatus.CONFLICT))TextButton(onClick={vm.onEvent(AccountSyncEvent.OpenCancel(job))}){Text("작업 중단")}}}
    }}}
}
private val creations=setOf(SyncKind.CREATE_ALBUM,SyncKind.CREATE_SUB,SyncKind.UPLOAD_PHOTO,SyncKind.CREATE_COMMENT)
private fun valueLabel(kind:SyncKind,value:String)=when(kind){SyncKind.DO_LIKE,SyncKind.UNDO_LIKE->if(value=="true")"좋아요"else "좋아요 취소";SyncKind.DELETE_ALBUM,SyncKind.DELETE_SUB,SyncKind.DELETE_PHOTO,SyncKind.DELETE_COMMENT->value.ifEmpty{"삭제"};SyncKind.SET_COVER->if(value.isEmpty())"대표 사진 변경"else "서버 대표 사진";SyncKind.CREATE_ALBUM,SyncKind.CREATE_SUB,SyncKind.UPLOAD_PHOTO,SyncKind.SET_DESCRIPTION,SyncKind.EDIT_ALBUM_TITLE,SyncKind.SET_SUBTITLE,SyncKind.EDIT_SUB,SyncKind.CREATE_COMMENT,SyncKind.EDIT_COMMENT->value.ifEmpty{"비어 있음"}}
private fun statusLabel(status:SyncStatus)=when(status){SyncStatus.QUEUED->"기기에 저장됨 · 동기화 대기";SyncStatus.PREPARING->"계정과 서버 상태 확인 중";SyncStatus.SENDING->"서버에 전송 중";SyncStatus.AWAITING_ID->"서버 생성 완료 · 항목 연결 확인 필요";SyncStatus.UNKNOWN->"반영 여부 확인 필요 · 자동 재전송 안 함";SyncStatus.CONFLICT->"서버 값과 내 변경 선택 필요";SyncStatus.AUTH_REQUIRED->"같은 계정으로 다시 로그인하면 이어갑니다";SyncStatus.PERMISSION_DENIED->"권한 확인 필요";SyncStatus.NOT_FOUND->"서버 대상을 찾을 수 없습니다";SyncStatus.DONE->"처리 완료";SyncStatus.CANCELLED->"중단됨"}
