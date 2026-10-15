package cord.eoeo.momentwo.feature.album.impl
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow

internal open class StrictAccountLibraryAccess : AccountLibraryAccess {
    override fun identities(): Flow<AccountIdentity?> = error("Unexpected identities")
    override suspend fun logout(): Unit = error("Unexpected logout")
    override suspend fun storage(): Pair<Long, Long> = error("Unexpected storage")
    override suspend fun refresh(): Unit = error("Unexpected refresh")
    override suspend fun refreshPermission(id: String): Unit = error("Unexpected refreshPermission")
    override suspend fun members(id: String): List<MemberItem> = error("Unexpected members")
    override suspend fun inviteMembers(id: String, names: List<String>): Unit = error("Unexpected inviteMembers")
    override suspend fun kickMembers(id: String, ids: Set<Int>): Unit = error("Unexpected kickMembers")
    override suspend fun leaveAlbum(id: String): Unit = error("Unexpected leaveAlbum")
    override suspend fun refreshAlbum(id: String): Unit = error("Unexpected refreshAlbum")
    override suspend fun refreshPhotos(id: String): Unit = error("Unexpected refreshPhotos")
    override suspend fun refreshPhoto(id: String): Unit = error("Unexpected refreshPhoto")
    override suspend fun refreshComments(id: String): Unit = error("Unexpected refreshComments")
}
