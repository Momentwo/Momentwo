package cord.eoeo.momentwo.data.signup

import cord.eoeo.momentwo.data.model.User

class SignUpRepositoryImpl(
    private val signUpRemoteDataSource: SignUpDataSource
) : SignUpRepository {
    override suspend fun requestSignUp(user: User): Result<Unit> {
        return signUpRemoteDataSource.requestSignUp(user)
    }
}
