package cord.eoeo.momentwo.feature.album.impl
import cord.eoeo.momentwo.core.data.library.*
import cord.eoeo.momentwo.core.model.*
import kotlinx.coroutines.flow.Flow

internal open class StrictAccountSyncOperations : AccountSyncOperations {
    override suspend fun requireOwner(expected: AccountIdentity): AccountIdentity = error("Unexpected requireOwner")
    override fun observeWorks(): Flow<List<SyncWork>> = error("Unexpected observeWorks")
    override suspend fun copyAlbums(ids: Set<String>, expected: AccountIdentity, intentId: String): String = error("Unexpected copyAlbums")
    override suspend fun schedulePending(): Unit = error("Unexpected schedulePending")
    override suspend fun createAccountAlbum(title: String, intentId: String, expected: AccountIdentity?, inviteNames: List<String>): String = error("Unexpected createAccountAlbum")
    override fun comments(photoId: String): Flow<List<AccountComment>> = error("Unexpected comments")
    override suspend fun setLiked(photoId: String, liked: Boolean): Unit = error("Unexpected setLiked")
    override suspend fun saveComment(photoId: String, text: String, id: String): Unit = error("Unexpected saveComment")
    override suspend fun deleteComment(id: String): Unit = error("Unexpected deleteComment")
    override suspend fun candidates(jobId: String, expected: AccountIdentity): List<RemoteCandidate> = error("Unexpected candidates")
    override suspend fun inspect(jobId: String, expected: AccountIdentity): String = error("Unexpected inspect")
    override suspend fun bind(jobId: String, candidateId: Int, expected: AccountIdentity): Unit = error("Unexpected bind")
    override suspend fun keepMine(jobId: String, expected: AccountIdentity): Unit = error("Unexpected keepMine")
    override suspend fun useServer(jobId: String, expected: AccountIdentity, observed: String): Unit = error("Unexpected useServer")
    override suspend fun retryConfirmed(jobId: String, expected: AccountIdentity): Unit = error("Unexpected retryConfirmed")
    override suspend fun cancel(jobId: String, expected: AccountIdentity): Unit = error("Unexpected cancel")
}
