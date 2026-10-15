package cord.eoeo.momentwo.core.data.signup

import cord.eoeo.momentwo.core.model.SignUpInput

interface SignUpRepository {
    suspend fun requestSignUp(input: SignUpInput): Result<Unit>

    suspend fun checkEmail(email: String): Result<Unit>

    suspend fun checkNickname(nickname: String): Result<Unit>
}
