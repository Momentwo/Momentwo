package cord.eoeo.momentwo.feature.album.impl.account
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
@Composable fun AccountAccessGuard(onAccountRequired:()->Unit,content: @Composable () -> Unit){AccountGuardRoute(onAccountRequired,content)}
@Composable private fun AccountGuardRoute(onAccountRequired:()->Unit,content: @Composable () -> Unit,vm:AccountGateViewModel=hiltViewModel()){
    val state by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.allowed,state.epoch,state.settled){if(state.settled && state.epoch!=null && !state.allowed)onAccountRequired()}
    if(!state.settled) CircularProgressIndicator() else if(state.allowed) content() else Column{Text("계정 연결이 필요한 기능입니다");TextButton(onClick=onAccountRequired){Text("계정 연결")}}
}
