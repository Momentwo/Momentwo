package cord.eoeo.momentwo.feature.albumdetail.impl
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow

internal open class StrictAccountLibraryAccess : AccountLibraryAccess {
    override fun identities(): Flow<AccountIdentity?> = throw AssertionError("Unexpected identities")
    override suspend fun logout(): Unit = throw AssertionError("Unexpected logout")
    override suspend fun storage(): Pair<Long, Long> = throw AssertionError("Unexpected storage")
    override suspend fun refresh(): Unit = throw AssertionError("Unexpected refresh")
    override suspend fun refreshPermission(id: String): Unit = throw AssertionError("Unexpected refreshPermission")
    override suspend fun members(id: String): List<MemberItem> = throw AssertionError("Unexpected members")
    override suspend fun inviteMembers(id: String, names: List<String>): Unit = throw AssertionError("Unexpected inviteMembers")
    override suspend fun kickMembers(id: String, ids: Set<Int>): Unit = throw AssertionError("Unexpected kickMembers")
    override suspend fun leaveAlbum(id: String): Unit = throw AssertionError("Unexpected leaveAlbum")
    override suspend fun refreshAlbum(id: String): Unit = throw AssertionError("Unexpected refreshAlbum")
    override suspend fun refreshPhotos(id: String): Unit = throw AssertionError("Unexpected refreshPhotos")
    override suspend fun refreshPhoto(id: String): Unit = throw AssertionError("Unexpected refreshPhoto")
    override suspend fun refreshComments(id: String): Unit = throw AssertionError("Unexpected refreshComments")
}
