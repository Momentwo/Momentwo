package cord.eoeo.momentwo.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val originToken: String,
    val refreshToken: String,
)
