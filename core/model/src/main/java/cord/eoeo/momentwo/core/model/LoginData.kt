package cord.eoeo.momentwo.core.model

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val profile: Profile,
)
