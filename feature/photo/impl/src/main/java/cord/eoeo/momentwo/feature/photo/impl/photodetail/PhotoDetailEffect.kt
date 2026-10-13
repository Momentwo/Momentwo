package cord.eoeo.momentwo.feature.photo.impl.photodetail

import cord.eoeo.momentwo.core.common.UiEffect

internal sealed interface PhotoDetailEffect : UiEffect {
    data object RefreshCommentPagingData : PhotoDetailEffect

    data object ScrollCommentToBottom : PhotoDetailEffect

    data object ScrollCommentToTop : PhotoDetailEffect

    data object PopBackStack : PhotoDetailEffect

    data class ShowSnackbar(
        val message: String,
    ) : PhotoDetailEffect
}
