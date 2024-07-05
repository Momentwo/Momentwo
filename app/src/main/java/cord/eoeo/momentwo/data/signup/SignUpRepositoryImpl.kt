package cord.eoeo.momentwo.data.signup

import cord.eoeo.momentwo.data.model.Email
import cord.eoeo.momentwo.data.model.Nickname
import cord.eoeo.momentwo.data.model.User

class SignUpRepositoryImpl(
    private val signUpRemoteDataSource: SignUpDataSource
) : SignUpRepository {
    override suspend fun requestSignUp(user: User): Result<Unit> {
        return Result.success(Unit)
        // return signUpRemoteDataSource.requestSignUp(user)
    }

    override suspend fun checkEmail(email: String): Result<Boolean> {
        return Result.success(true)
        // return signUpRemoteDataSource.checkEmail(Email(email))
    }

    override suspend fun checkNickname(nickname: String): Result<Boolean> {
        return Result.success(true)
        // return signUpRemoteDataSource.checkNickname(Nickname(nickname))
    }
}
