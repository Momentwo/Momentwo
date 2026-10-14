package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
internal data class LocalPhotoContent(val photo: LocalPhoto, val memos: List<LocalMemo>)
internal sealed interface LocalPhotoDialog {
    data object None : LocalPhotoDialog
    data object Caption : LocalPhotoDialog
    data class Memo(val id: String?) : LocalPhotoDialog
    data object Delete : LocalPhotoDialog
}
internal data class LocalPhotoState(val id: String, val screen: ScreenState<LocalPhotoContent> = ScreenState.Loading, val dialog: LocalPhotoDialog = LocalPhotoDialog.None, val draft: LibraryDraft = LibraryDraft("", "", ""), val interaction: Long = 0, val pending: Set<String> = emptySet(), val immersive: Boolean = false) : UiState
