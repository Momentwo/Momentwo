package cord.eoeo.momentwo.feature.album.impl.account
import cord.eoeo.momentwo.core.common.UiState
import cord.eoeo.momentwo.core.model.AccountIdentity
internal data class AccountGateState(val identity:AccountIdentity?=null,val settled:Boolean=false,val epoch:String?=null):UiState{val allowed get()=identity!=null && identity.epoch==epoch}
