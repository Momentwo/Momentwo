package cord.eoeo.momentwo.core.data.signup

import cord.eoeo.momentwo.core.model.SignUpInput
import cord.eoeo.momentwo.core.common.runSuspendCatching

internal class SignUpRepositoryImpl(
    private val signUpRemoteDataSource: SignUpDataSource
) : SignUpRepository {
    override suspend fun requestSignUp(input: SignUpInput): Result<Unit> = runSuspendCatching {
        signUpRemoteDataSource.requestSignUp(input.toUserDto()).getOrThrow()
    }

    override suspend fun checkEmail(email: String): Result<Unit> {
        return signUpRemoteDataSource.checkEmail(email)
    }

    override suspend fun checkNickname(nickname: String): Result<Unit> {
        return signUpRemoteDataSource.checkNickname(nickname)
    }
}
