package cord.eoeo.momentwo.data.signup

import cord.eoeo.momentwo.data.model.User

class SignUpRepositoryImpl(
    private val signUpRemoteDataSource: SignUpDataSource
) : SignUpRepository {
    override suspend fun requestSignUp(user: User): Result<Unit> {
        // return Result.success(Unit)
        return signUpRemoteDataSource.requestSignUp(user)
    }

    override suspend fun checkEmail(email: String): Result<Unit> {
        // return Result.success(true)
        return signUpRemoteDataSource.checkEmail(email)
    }

    override suspend fun checkNickname(nickname: String): Result<Unit> {
        // return Result.success(true)
        return signUpRemoteDataSource.checkNickname(nickname)
    }
}
