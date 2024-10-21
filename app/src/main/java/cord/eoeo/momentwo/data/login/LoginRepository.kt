package cord.eoeo.momentwo.data.login

import cord.eoeo.momentwo.ui.model.TokenItem

interface LoginRepository {
    suspend fun requestLogin(email: String, password: String): Result<TokenItem>
}
