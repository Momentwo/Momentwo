package cord.eoeo.momentwo.feature.album.impl.local
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
internal data class LibraryCreateState(val ready: Boolean = false, val restoreFailed: Boolean = false, val ownerAvailable: Boolean = true, val account: Boolean = false, val identity: AccountIdentity? = null,
    val draft: LibraryDraft = LibraryDraft("", "", ""), val selected: List<String> = emptyList(), val friends: ScreenState<List<FriendItem>> = ScreenState.Loading,
    val temporary: List<String> = emptyList(), val inviteOpen: Boolean = false, val pending: Boolean = false, val revision: Long = 0) : UiState
