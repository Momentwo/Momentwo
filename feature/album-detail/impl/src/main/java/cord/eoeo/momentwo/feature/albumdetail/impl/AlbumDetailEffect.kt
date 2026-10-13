package cord.eoeo.momentwo.feature.albumdetail.impl

import android.net.Uri
import cord.eoeo.momentwo.core.model.FriendItem
import cord.eoeo.momentwo.core.model.MemberItem
import cord.eoeo.momentwo.core.common.UiEffect

internal sealed interface AlbumDetailEffect : UiEffect {
    data object PopBackStack : AlbumDetailEffect

    data object PopBackStackInAlbumDetail : AlbumDetailEffect

    data class ShowSnackbar(
        val message: String,
    ) : AlbumDetailEffect
}
