package cord.eoeo.momentwo.feature.album.impl.account
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
import cord.eoeo.momentwo.core.ui.*
import cord.eoeo.momentwo.feature.album.api.AccountLibraryDestination
import kotlinx.coroutines.launch
fun NavGraphBuilder.accountLibraryScreen(back:()->Unit,login:()->Unit,friend:()->Unit,open:(String)->Unit,sync:()->Unit){composable<AccountLibraryDestination>{AccountLibraryRoute(back,login,friend,open,sync)}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AccountLibraryRoute(back:()->Unit,login:()->Unit,friend:()->Unit,open:(String)->Unit,sync:()->Unit,vm:AccountLibraryViewModel=hiltViewModel()){
    val state by vm.uiState.collectAsStateWithLifecycle();val messages=remember{SnackbarHostState()};LaunchedEffect(vm){vm.initialize()}
    CollectEffect(vm.effect){when(val e=it){AccountLibraryEffect.Back->back();AccountLibraryEffect.Login->login();AccountLibraryEffect.Friend->friend();AccountLibraryEffect.Sync->sync();is AccountLibraryEffect.Open->open(e.id);is AccountLibraryEffect.Message->launch{messages.showSnackbar(e.text)}}}
    Scaffold(topBar={TopAppBar(title={Text("계정·저장 공간")},navigationIcon={TextButton(onClick={vm.onEvent(AccountLibraryEvent.Back)}){Text("뒤로")}})},snackbarHost={SnackbarHost(messages)}){padding->ScreenContent(state.screen,{vm.onEvent(AccountLibraryEvent.Retry)},Modifier.padding(padding)){content->LazyColumn(Modifier.fillMaxSize().padding(16.dp)){
        item{Text("이 기기 · 앨범 ${content.deviceAlbums}개");Text("사진 보관 ${(content.usedBytes/1024/1024)} MB · 남은 공간 ${(content.availableBytes/1024/1024)} MB");Text("기기 자료는 계정 연결·해제와 관계없이 유지됩니다")
            if(content.identity==null){Text("계정 없이 사용 중");TextButton(onClick={vm.onEvent(AccountLibraryEvent.Login)}){Text("로그인")};Text("클라우드·친구·공유를 사용할 때 계정을 연결하세요")}
            else{Text(content.identity.accountId);if(!content.identity.canSync){Text("계정 인증이 만료되었습니다. 기기 앨범과 저장된 계정 자료를 계속 사용할 수 있습니다");TextButton(onClick={vm.onEvent(AccountLibraryEvent.Login)}){Text("다시 로그인")}}
                Text("로그인만으로 기기 자료를 업로드하지 않습니다");TextButton(onClick={vm.onEvent(AccountLibraryEvent.Sync)}){Text("기기 앨범 복사·동기화")}
                Row{TextButton(onClick={vm.onEvent(AccountLibraryEvent.Retry)},enabled=content.identity.canSync){Text("계정 앨범 불러오기")};TextButton(onClick={vm.onEvent(AccountLibraryEvent.Friend)}){Text("친구·공유")}}
                TextButton(onClick={vm.onEvent(AccountLibraryEvent.Logout)},enabled="logout" !in state.pending){Text("로그아웃")}
                if(content.albums.isEmpty())Text("저장된 계정 앨범이 없습니다")
            }
        }
        items(content.albums,key={it.localId}){album->TextButton(onClick={vm.onEvent(AccountLibraryEvent.Open(album.localId))}){Text(album.title)}}
    }}}
}
