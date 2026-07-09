package cord.eoeo.momentwo.core.data.model

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

@JsonClass(generateAdapter = true)
data class UserProfile(
    val name: String,
    val username: String,
    val nickname: String,
    val phone: String,
    val userProfileImage: String,
)
