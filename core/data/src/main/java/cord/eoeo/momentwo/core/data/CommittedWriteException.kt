package cord.eoeo.momentwo.core.data

/** 원격 쓰기는 완료됐지만 후속 로컬 갱신이 실패한 경우. 원격 작업을 롤백/실패로 표시하지 않는다. */
class CommittedWriteException(cause: Throwable) : Exception("Remote write committed; local update failed", cause)
