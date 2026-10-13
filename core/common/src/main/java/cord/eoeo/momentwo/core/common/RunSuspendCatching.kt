package cord.eoeo.momentwo.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** 취소는 실패 데이터가 아니라 호출 coroutine의 제어 흐름으로 전달한다. */
suspend fun <T> runSuspendCatching(block: suspend () -> T): Result<T> {
    currentCoroutineContext().ensureActive()
    val result = runCatching { block() }
    result.exceptionOrNull()?.let { exception ->
        if (exception is CancellationException || exception is Error) throw exception
    }
    // 하위 계층이 취소를 삼키고 값을 반환한 경우에도 후속 처리를 차단한다.
    currentCoroutineContext().ensureActive()
    return result
}
