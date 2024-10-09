package cord.eoeo.momentwo.data.signup.remote

import cord.eoeo.momentwo.data.model.User
import cord.eoeo.momentwo.data.signup.SignUpDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignUpRemoteDataSource(
    private val signUpService: SignUpService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SignUpDataSource {
    override suspend fun requestSignUp(user: User): Result<Unit> = runCatching {
        withContext(dispatcher) {
            signUpService.postSignUp(user)
        }
    }

    override suspend fun checkEmail(email: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            signUpService.postCheckEmail(email).isSuccessful
        }
    }

    override suspend fun checkNickname(nickname: String): Result<Unit> = runCatching {
        withContext(dispatcher) {
            signUpService.postCheckNickname(nickname).isSuccessful
        }
    }
}
