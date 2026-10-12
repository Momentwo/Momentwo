package cord.eoeo.momentwo.feature.auth.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Retain the serialized identity used by previously saved Navigation back stacks.
@Serializable
@SerialName("cord.eoeo.momentwo.core.navigation.MomentwoDestination.Login")
data object LoginDestination

@Serializable
@SerialName("cord.eoeo.momentwo.core.navigation.MomentwoDestination.SignUp")
data object SignUpDestination

@Serializable
@SerialName("cord.eoeo.momentwo.core.navigation.MomentwoDestination.Profile")
data class ProfileDestination(val nickname: String?)
