package cord.eoeo.momentwo.feature.album.impl.account
import cord.eoeo.momentwo.core.common.Reducer
internal object AccountGateReducer:Reducer<AccountGateState,AccountGateAction>{override fun reduce(state:AccountGateState,action:AccountGateAction)=state.copy(identity=action.identity,settled=true,epoch=state.epoch ?: action.identity?.epoch)}
