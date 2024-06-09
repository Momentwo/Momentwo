package cord.eoeo.momentwo.data.signup.remote

import cord.eoeo.momentwo.data.model.User
import cord.eoeo.momentwo.data.signup.SignUpDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignUpRemoteDataSource(
    private val signUpService: SignUpService
) : SignUpDataSource {
    override suspend fun requestSignUp(user: User): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            signUpService.postSignUp(user)
        }
    }
}
