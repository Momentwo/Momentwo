package cord.eoeo.momentwo.feature.album.impl.account
import androidx.lifecycle.viewModelScope
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.data.library.AccountLibraryAccess
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
@HiltViewModel
internal class AccountGateViewModel @Inject constructor(accounts:AccountLibraryAccess):ReducerViewModel<AccountGateState,AccountGateEvent,AccountGateAction,AccountGateEffect>(AccountGateState(),AccountGateReducer){init{viewModelScope.launch{accounts.identities().catch{if(it is CancellationException || it is Error)throw it;emit(null)}.collect{dispatch(AccountGateAction(it))}}};override fun handleEvent(event:AccountGateEvent)=Unit}
