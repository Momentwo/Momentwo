package cord.eoeo.momentwo.feature.albumdetail.impl

import android.net.Uri
import cord.eoeo.momentwo.core.model.FriendItem
import cord.eoeo.momentwo.core.model.MemberItem

internal sealed interface MemberEvent {
    data object Exit : MemberEvent

    data object GetMembers : MemberEvent

    data class KickMembers(
        val kickMemberIds: List<Int>,
    ) : MemberEvent

    data class AssignAdmin(
        val nickname: String,
    ) : MemberEvent

    data class EditPermissions(
        val editMembers: List<MemberItem>,
    ) : MemberEvent
}
