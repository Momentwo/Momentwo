package cord.eoeo.momentwo.core.data.library

import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow

/** UI 업무의 주입 경계. 저장·인증·취소 정책은 기존 구현이 소유한다. */
interface AccountSyncOperations {
    suspend fun requireOwner(expected: AccountIdentity): AccountIdentity
    fun observeWorks(): Flow<List<SyncWork>>
    suspend fun copyAlbums(ids: Set<String>, expected: AccountIdentity, intentId: String): String
    suspend fun schedulePending()
    suspend fun createAccountAlbum(title: String, intentId: String, expected: AccountIdentity? = null, inviteNames: List<String> = emptyList()): String
    fun comments(photoId: String): Flow<List<AccountComment>>
    suspend fun setLiked(photoId: String, liked: Boolean)
    suspend fun saveComment(photoId: String, text: String, id: String)
    suspend fun deleteComment(id: String)
    suspend fun candidates(jobId: String, expected: AccountIdentity): List<RemoteCandidate>
    suspend fun inspect(jobId: String, expected: AccountIdentity): String
    suspend fun bind(jobId: String, candidateId: Int, expected: AccountIdentity)
    suspend fun keepMine(jobId: String, expected: AccountIdentity)
    suspend fun useServer(jobId: String, expected: AccountIdentity, observed: String)
    suspend fun retryConfirmed(jobId: String, expected: AccountIdentity)
    suspend fun cancel(jobId: String, expected: AccountIdentity)
}
