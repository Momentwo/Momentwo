package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.UiEffect
internal sealed interface LocalPhotoListEffect : UiEffect {
    data object Back : LocalPhotoListEffect
    data class Open(val id: String) : LocalPhotoListEffect
    data class Message(val text: String) : LocalPhotoListEffect
}
