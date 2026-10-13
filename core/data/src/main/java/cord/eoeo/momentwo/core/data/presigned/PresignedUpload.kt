package cord.eoeo.momentwo.core.data.presigned

import cord.eoeo.momentwo.core.common.runSuspendCatching

/** Each dependency Result is unwrapped before starting the next remote step. */
internal suspend fun commitPresignedUpload(
    requestUrl: suspend () -> Result<String>,
    upload: suspend (String) -> Result<Unit>,
    commit: suspend (String) -> Result<Unit>,
): Result<Unit> = runSuspendCatching {
    val url = runSuspendCatching { requestUrl().getOrThrow() }.getOrThrow()
    runSuspendCatching { upload(url).getOrThrow() }.getOrThrow()
    // Preserve the existing S3 object-key wire format.
    val filename = url.split("?").first().split("/").drop(3).joinToString("/")
    runSuspendCatching { commit(filename).getOrThrow() }.getOrThrow()
}
