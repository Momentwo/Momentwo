package cord.eoeo.momentwo.data.signup

import cord.eoeo.momentwo.data.model.Email
import cord.eoeo.momentwo.data.model.Nickname
import cord.eoeo.momentwo.data.model.User

interface SignUpDataSource {
    suspend fun requestSignUp(user: User): Result<Unit>

    suspend fun checkEmail(email: Email): Result<Boolean>

    suspend fun checkNickname(nickname: Nickname): Result<Boolean>
}
