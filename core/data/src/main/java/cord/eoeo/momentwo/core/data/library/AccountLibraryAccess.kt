package cord.eoeo.momentwo.core.data.library

import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow

/** UI 업무의 주입 경계. 저장·인증·취소 정책은 기존 구현이 소유한다. */
interface AccountLibraryAccess {
    fun identities(): Flow<AccountIdentity?>
    suspend fun logout()
    suspend fun storage(): Pair<Long, Long>
    suspend fun refresh()
    suspend fun refreshPermission(id: String)
    suspend fun members(id: String): List<MemberItem>
    suspend fun inviteMembers(id: String, names: List<String>)
    suspend fun kickMembers(id: String, ids: Set<Int>)
    suspend fun leaveAlbum(id: String)
    suspend fun refreshAlbum(id: String)
    suspend fun refreshPhotos(id: String)
    suspend fun refreshPhoto(id: String)
    suspend fun refreshComments(id: String)
}
