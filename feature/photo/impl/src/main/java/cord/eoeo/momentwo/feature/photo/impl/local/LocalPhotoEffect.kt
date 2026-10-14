package cord.eoeo.momentwo.feature.photo.impl.local
import cord.eoeo.momentwo.core.common.UiEffect
internal sealed interface LocalPhotoEffect : UiEffect {
    data object Back : LocalPhotoEffect
    data class Message(val text: String) : LocalPhotoEffect
}
