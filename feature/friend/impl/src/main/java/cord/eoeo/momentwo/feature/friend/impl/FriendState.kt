package cord.eoeo.momentwo.feature.friend.impl

import androidx.paging.PagingData
import cord.eoeo.momentwo.core.common.*
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal enum class RequestTab { Received, Sent }
internal sealed interface FriendWriteKey : RequestKey {
    data class Send(val nickname: String) : FriendWriteKey
    data class Respond(val userId: Int) : FriendWriteKey
    data class Cancel(val userId: Int) : FriendWriteKey
}
internal data class FriendRequestsContent(val requests: List<FriendRequestItem>)
internal data class FriendState(
    val selectedNavIndex: Int = 0,
    val requestTab: RequestTab = RequestTab.Received,
    val isRequestDialogOpened: Boolean = false,
    val dialogRevision: Long = 0,
    val searchQuery: String = "",
    val friendPagingSource: ScreenState<Flow<PagingData<FriendItem>>> = ScreenState.Loading,
    val searchUserPagingData: Flow<PagingData<UserItem>> = emptyFlow(),
    val received: ScreenState<FriendRequestsContent> = ScreenState.Loading,
    val sent: ScreenState<FriendRequestsContent> = ScreenState.Loading,
    val receivedVersion: Long = 0,
    val sentVersion: Long = 0,
    val receivedDirty: Boolean = true,
    val sentDirty: Boolean = true,
    val friendListRevision: Long = 0,
    val acceptedRequests: Set<Int> = emptySet(),
    val pendingWrites: Set<FriendWriteKey> = emptySet(),
) : UiState {
    fun requests(tab: RequestTab) = when (tab) { RequestTab.Received -> received; RequestTab.Sent -> sent }
    fun version(tab: RequestTab) = when (tab) { RequestTab.Received -> receivedVersion; RequestTab.Sent -> sentVersion }
    fun dirty(tab: RequestTab) = when (tab) { RequestTab.Received -> receivedDirty; RequestTab.Sent -> sentDirty }
}
