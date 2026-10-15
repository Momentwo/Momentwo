package cord.eoeo.momentwo.core.testing

import kotlinx.coroutines.CompletableDeferred

/** suspend 요청을 실제 시간 없이 완료·실패·취소한다. 인자는 Fake에서 기록한다. */
class ControlledCall<T> {
    private val response = CompletableDeferred<T>()
    suspend fun await(): T = response.await()
    fun complete(value: T) { check(response.complete(value)) }
    fun fail(error: Throwable) { check(response.completeExceptionally(error)) }
    fun cancel() = response.cancel()
}
