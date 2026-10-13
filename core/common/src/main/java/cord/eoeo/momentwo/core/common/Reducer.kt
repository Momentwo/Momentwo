package cord.eoeo.momentwo.core.common

fun interface Reducer<State, Action> {
    fun reduce(state: State, action: Action): State
}
