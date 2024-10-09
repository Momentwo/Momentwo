package cord.eoeo.momentwo.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val name: String,
    val username: String,
    val password: String,
    val nickname: String,
    val birthday: String,
    val phone: String,
)
