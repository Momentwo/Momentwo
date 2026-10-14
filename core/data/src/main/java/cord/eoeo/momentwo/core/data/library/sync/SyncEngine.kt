package cord.eoeo.momentwo.core.data.library.sync

import cord.eoeo.momentwo.core.common.runSuspendCatching
import cord.eoeo.momentwo.core.database.SyncJobEntity
import cord.eoeo.momentwo.core.model.*
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

internal interface SyncWorkStore {
    suspend fun recover(accountId: String)
    suspend fun next(accountId: String): SyncJobEntity?
    suspend fun claim(job: SyncJobEntity, claim: String): Boolean
    suspend fun sending(job: SyncJobEntity, claim: String): Boolean
    suspend fun settle(job: SyncJobEntity, claim: String, status: SyncStatus, remote: String? = null, extra: String? = null)
    suspend fun current(id: String): SyncJobEntity?
    suspend fun interrupted(job: SyncJobEntity, claim: String)
}
internal sealed interface Preparation {
    data object Ready : Preparation
    data object AlreadyApplied : Preparation
    data object Waiting : Preparation
    data class Conflict(val text: String, val extra: String? = null) : Preparation
}
internal enum class WriteReceipt { Applied, NeedsId }
internal interface SyncTransport {
    suspend fun prepare(job: SyncJobEntity): Preparation
    suspend fun execute(job: SyncJobEntity): WriteReceipt
}

/** No timers, credentials, UI effects or retries of uncertain writes. The durable store owns each attempt. */
internal class SyncEngine(private val store: SyncWorkStore) {
    private val lock = Mutex()
    suspend fun run(accountId: String, transport: SyncTransport): Boolean = lock.withLock {
        store.recover(accountId)
        var retryRead = false
        while (true) {
            currentCoroutineContext().ensureActive()
            val job = store.next(accountId) ?: break
            check(job.accountId == accountId)
            val claim = UUID.randomUUID().toString()
            if (!store.claim(job, claim)) continue
            try {
                val prepared = runSuspendCatching { transport.prepare(job) }
                currentCoroutineContext().ensureActive()
                val failure = prepared.exceptionOrNull()
                if (failure != null) {
                    val status = rejection(failure) ?: SyncStatus.QUEUED
                    store.settle(job,claim,status)
                    retryRead = status == SyncStatus.QUEUED
                    break
                }
                when (val readiness = prepared.getOrThrow()) {
                    Preparation.AlreadyApplied -> store.settle(job,claim,SyncStatus.DONE)
                    is Preparation.Conflict -> store.settle(job,claim,SyncStatus.CONFLICT,readiness.text,readiness.extra)
                    Preparation.Waiting -> { store.settle(job,claim,SyncStatus.QUEUED); break }
                    Preparation.Ready -> {
                        if (!store.sending(job,claim)) continue
                        val written = runSuspendCatching { transport.execute(job) }
                        currentCoroutineContext().ensureActive()
                        written.fold(onSuccess = { receipt -> store.settle(job,claim,if(receipt == WriteReceipt.NeedsId) SyncStatus.AWAITING_ID else SyncStatus.DONE) },onFailure = { error ->
                            val current = store.current(job.id)
                            val recordMayBeSent = current == null || SyncKind.valueOf(current.kind) !in setOf(SyncKind.UPLOAD_PHOTO,SyncKind.SET_COVER) || current.mediaName==null || current.transferPhase >= 2
                            // The final record attempt is persisted before the HTTP mutation.
                            val status = rejection(error) ?: if(recordMayBeSent) SyncStatus.UNKNOWN else SyncStatus.QUEUED
                            store.settle(job,claim,status)
                            retryRead = status == SyncStatus.QUEUED
                        })
                        if (written.isFailure) break
                    }
                }
            } catch(cancelled: CancellationException) {
                withContext(NonCancellable) { store.interrupted(job,claim) }
                throw cancelled
            }
        }
        retryRead
    }
    private fun rejection(error: Throwable): SyncStatus? = when((error as? HttpException)?.code()) {
        401 -> SyncStatus.AUTH_REQUIRED
        403 -> SyncStatus.PERMISSION_DENIED
        404 -> SyncStatus.NOT_FOUND
        null, 400, 409, 429, 500, 502, 503, 504 -> null
        else -> null
    }
}
