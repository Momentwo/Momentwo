package cord.eoeo.momentwo.feature.photo.impl.photolist

import android.net.Uri
import cord.eoeo.momentwo.core.common.UiEffect

internal sealed interface PhotoListEffect : UiEffect {
    data object RefreshPagingData : PhotoListEffect

    data object PopBackStack : PhotoListEffect

    data class ShowSnackbar(
        val message: String,
    ) : PhotoListEffect
}
