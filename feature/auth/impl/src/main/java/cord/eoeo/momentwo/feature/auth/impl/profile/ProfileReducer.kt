package cord.eoeo.momentwo.feature.auth.impl.profile

import cord.eoeo.momentwo.core.common.Reducer

// 현재 Profile에는 상태를 변경하는 기능이 없다. #161에서 기능과 Action을 추가한다.
internal object ProfileReducer : Reducer<ProfileState, Nothing> {
    override fun reduce(state: ProfileState, action: Nothing): ProfileState = state
}
