package cord.eoeo.momentwo.feature.albumdetail.impl

import android.net.Uri
import cord.eoeo.momentwo.core.model.FriendItem
import cord.eoeo.momentwo.core.model.MemberItem

internal sealed interface SubAlbumEvent {
    data class Create(
        val subAlbumTitle: String,
    ) : SubAlbumEvent

    data object GetSubAlbums : SubAlbumEvent

    data class EditTitle(
        val subAlbumId: Int,
        val title: String,
    ) : SubAlbumEvent

    data class DeleteSubAlbums(
        val subAlbumIds: List<Int>,
    ) : SubAlbumEvent
}
