package cord.eoeo.momentwo.core.common

import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface RequestKey

enum class RequestPolicy { LatestWins, IgnoreWhileRunning }

/** ViewModel별로 소유한다. 등록·취소·콜백은 Main에서 처리하며 업무 로직은 보관하지 않는다. */
@MainThread
class RequestController(private val scope: CoroutineScope) {
    private data class Entry(val id: Long, val job: Job)
    private val entries = mutableMapOf<RequestKey, Entry>()
    private var sequence = 0L
    private var closed = false

    fun isRunning(key: RequestKey): Boolean = entries[key]?.job?.isActive == true

    fun cancel(key: RequestKey) { entries[key]?.job?.cancel() }

    fun close() {
        closed = true
        val jobs = entries.values.map { it.job }
        entries.clear()
        jobs.forEach { it.cancel() }
    }

    fun <T> launchRequest(
        key: RequestKey,
        policy: RequestPolicy,
        request: suspend () -> Result<T>,
        onStarted: () -> Unit = {},
        onSuccess: suspend (T) -> Unit,
        onFailure: suspend (Throwable) -> Unit,
        onFinished: () -> Unit = {},
    ): Job? {
        if (closed || !scope.isActive) return null
        val previous = entries[key]
        if (policy == RequestPolicy.IgnoreWhileRunning && previous?.job?.isActive == true) return null
        val id = ++sequence
        val job = scope.launch(start = CoroutineStart.LAZY) {
            // getOrThrow unwraps Result.failure(CancellationException) before it can reach UI callbacks.
            val result = runSuspendCatching { request().getOrThrow() }
            currentCoroutineContext().ensureActive()
            if (entries[key]?.id != id || closed) return@launch
            // Callbacks deliberately run outside runSuspendCatching.
            result.fold(onSuccess = { onSuccess(it) }, onFailure = { onFailure(it) })
        }
        entries[key] = Entry(id, job)
        job.invokeOnCompletion {
            if (entries[key]?.id == id) {
                entries.remove(key)
                if (!closed && scope.isActive) onFinished()
            }
        }
        // Replace the entry before cancelling: an old completion cannot finish the new request.
        previous?.job?.cancel()
        try {
            onStarted()
            job.start()
        } catch (exception: Throwable) {
            job.cancel()
            throw exception
        }
        return job
    }
}
