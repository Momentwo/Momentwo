package cord.eoeo.momentwo.feature.albumdetail.impl

import android.net.Uri
import cord.eoeo.momentwo.core.model.FriendItem
import cord.eoeo.momentwo.core.model.MemberItem

internal sealed interface AlbumSettingEvent {
    data object DeleteAlbum : AlbumSettingEvent

    data object ChangeImage : AlbumSettingEvent

    data class EditSubTitle(
        val subTitle: String,
    ) : AlbumSettingEvent

    data object DeleteSubTitle : AlbumSettingEvent

    data class EditTitle(
        val title: String,
    ) : AlbumSettingEvent
}
