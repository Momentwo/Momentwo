package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.model.*
internal sealed interface LibraryCreateAction {
    data class Restore(val draft: LibraryDraft, val identity: AccountIdentity?, val names: List<String>) : LibraryCreateAction
    data class Edit(val draft: LibraryDraft, val names: List<String>) : LibraryCreateAction
    data class Pending(val value: Boolean) : LibraryCreateAction
    data class Invite(val open: Boolean) : LibraryCreateAction
    data class Friends(val rows: List<FriendItem>) : LibraryCreateAction
    data class Check(val name: String, val checked: Boolean) : LibraryCreateAction
    data object FriendsFailed : LibraryCreateAction
    data class Reset(val token: String) : LibraryCreateAction
}
