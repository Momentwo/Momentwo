package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.UiError
internal sealed interface LocalPhotoListAction {
    data class Snapshot(val content: LocalPhotoListContent) : LocalPhotoListAction
    data class Failed(val error: UiError) : LocalPhotoListAction
    data class Importing(val value: Boolean) : LocalPhotoListAction
}
