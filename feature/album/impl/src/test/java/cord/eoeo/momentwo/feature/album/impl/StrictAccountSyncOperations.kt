package cord.eoeo.momentwo.feature.album.impl
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow

internal open class StrictAccountSyncOperations : AccountSyncOperations {
    override suspend fun requireOwner(expected: AccountIdentity): AccountIdentity = throw AssertionError("Unexpected requireOwner")
    override fun observeWorks(): Flow<List<SyncWork>> = throw AssertionError("Unexpected observeWorks")
    override suspend fun copyAlbums(ids: Set<String>, expected: AccountIdentity, intentId: String): String = throw AssertionError("Unexpected copyAlbums")
    override suspend fun schedulePending(): Unit = throw AssertionError("Unexpected schedulePending")
    override suspend fun createAccountAlbum(title: String, intentId: String, expected: AccountIdentity?, inviteNames: List<String>): String = throw AssertionError("Unexpected createAccountAlbum")
    override fun comments(photoId: String): Flow<List<AccountComment>> = throw AssertionError("Unexpected comments")
    override suspend fun setLiked(photoId: String, liked: Boolean): Unit = throw AssertionError("Unexpected setLiked")
    override suspend fun saveComment(photoId: String, text: String, id: String): Unit = throw AssertionError("Unexpected saveComment")
    override suspend fun deleteComment(id: String): Unit = throw AssertionError("Unexpected deleteComment")
    override suspend fun candidates(jobId: String, expected: AccountIdentity): List<RemoteCandidate> = throw AssertionError("Unexpected candidates")
    override suspend fun inspect(jobId: String, expected: AccountIdentity): String = throw AssertionError("Unexpected inspect")
    override suspend fun bind(jobId: String, candidateId: Int, expected: AccountIdentity): Unit = throw AssertionError("Unexpected bind")
    override suspend fun keepMine(jobId: String, expected: AccountIdentity): Unit = throw AssertionError("Unexpected keepMine")
    override suspend fun useServer(jobId: String, expected: AccountIdentity, observed: String): Unit = throw AssertionError("Unexpected useServer")
    override suspend fun retryConfirmed(jobId: String, expected: AccountIdentity): Unit = throw AssertionError("Unexpected retryConfirmed")
    override suspend fun cancel(jobId: String, expected: AccountIdentity): Unit = throw AssertionError("Unexpected cancel")
}
