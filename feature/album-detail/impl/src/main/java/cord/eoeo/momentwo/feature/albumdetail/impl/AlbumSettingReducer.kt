package cord.eoeo.momentwo.feature.albumdetail.impl

import cord.eoeo.momentwo.core.common.*

internal object AlbumSettingReducer : Reducer<AlbumDetailState, SettingAction> {
    override fun reduce(state: AlbumDetailState, action: SettingAction): AlbumDetailState = when (action) {
        SettingAction.Started -> state.copy(settings = state.settings.startRefresh())
        is SettingAction.Loaded -> state.copy(settings = ScreenState.Success(action.content))
        is SettingAction.Failed -> state.copy(settings = state.settings.failRefresh(action.error))
        SettingAction.Finished -> state.copy(settings = state.settings.cancelRefresh())
        is SettingAction.OpenDialog -> state.copy(dialog = action.dialog, dialogRevision = state.dialogRevision + 1, dialogSession = state.dialogSession + 1)
        SettingAction.DialogEdited -> state.copy(dialogRevision = state.dialogRevision + 1)
        is SettingAction.CloseSubmittedDialog -> if (state.dialogRevision == action.revision) state.copy(dialog = AlbumDetailDialog.None) else state
        is SettingAction.TitleChanged -> reduce(state.copy(albumItem = state.albumItem.copy(title = action.title)), SettingAction.CloseSubmittedDialog(action.revision))
        is SettingAction.SubtitleChanged -> reduce(state.copy(albumItem = state.albumItem.copy(subTitle = action.subtitle)), SettingAction.CloseSubmittedDialog(action.revision))
        is SettingAction.ImageScreenChanged -> state.copy(isInChangeImage = action.open, imageRevision = state.imageRevision + 1)
        is SettingAction.ImageSelected -> state.copy(imageUri = action.uri, imageRevision = state.imageRevision + 1)
        SettingAction.ImageCancelled -> state.copy(imageUri = state.committedImageUri, isInChangeImage = false, imageRevision = state.imageRevision + 1)
        is SettingAction.ImageSaved -> state.copy(committedImageUri = action.uri,
            isInChangeImage = if (state.imageRevision == action.revision) false else state.isInChangeImage)
    }
}
