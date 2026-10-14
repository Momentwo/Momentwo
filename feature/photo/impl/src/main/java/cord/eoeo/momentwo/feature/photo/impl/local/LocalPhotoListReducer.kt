package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.*
internal object LocalPhotoListReducer : Reducer<LocalPhotoListState, LocalPhotoListAction> {
    override fun reduce(state: LocalPhotoListState, action: LocalPhotoListAction) = when(action) {
        is LocalPhotoListAction.Snapshot -> state.copy(screen = ScreenState.Success(action.content))
        is LocalPhotoListAction.Failed -> state.copy(screen = if(action.error == UiError.NotFound) ScreenState.Error(action.error) else state.screen.failRefresh(action.error))
        is LocalPhotoListAction.Importing -> state.copy(importing = action.value)
    }
}
