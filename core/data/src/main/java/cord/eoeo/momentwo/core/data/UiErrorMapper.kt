package cord.eoeo.momentwo.core.data

import cord.eoeo.momentwo.core.common.UiError
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

fun Throwable.toUiError(): UiError = when (this) {
    is CancellationException -> throw this
    is Error -> throw this
    is IOException -> UiError.Network
    is HttpException -> when (code()) {
        401 -> UiError.Unauthorized
        403 -> UiError.Forbidden
        404 -> UiError.NotFound
        in 500..599 -> UiError.Server
        else -> UiError.Unknown
    }
    else -> UiError.Unknown
}
