package cord.eoeo.momentwo.core.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** 하나의 collector로 순서·횟수와 추가 발행 없음을 검증한다. */
class EffectRecorder<T>(flow: Flow<T>, scope: CoroutineScope) {
    private val recorded = mutableListOf<T>()
    val values: List<T> get() = recorded.toList()
    private val job: Job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
        flow.collect { recorded += it }
    }
    fun cancel() = job.cancel()
}
