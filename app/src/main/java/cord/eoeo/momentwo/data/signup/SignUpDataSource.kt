package cord.eoeo.momentwo.data.signup

import cord.eoeo.momentwo.data.model.User

interface SignUpDataSource {
    suspend fun requestSignUp(user: User): Result<Unit>
}
