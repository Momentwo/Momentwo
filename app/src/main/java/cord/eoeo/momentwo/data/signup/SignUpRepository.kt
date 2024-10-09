package cord.eoeo.momentwo.data.signup

import cord.eoeo.momentwo.data.model.User

interface SignUpRepository {
    suspend fun requestSignUp(user: User): Result<Unit>

    suspend fun checkEmail(email: String): Result<Unit>

    suspend fun checkNickname(nickname: String): Result<Unit>
}
