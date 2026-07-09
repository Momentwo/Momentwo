package cord.eoeo.momentwo.core.data.signup

import cord.eoeo.momentwo.core.data.model.User

interface SignUpRepository {
    suspend fun requestSignUp(user: User): Result<Unit>

    suspend fun checkEmail(email: String): Result<Unit>

    suspend fun checkNickname(nickname: String): Result<Unit>
}
