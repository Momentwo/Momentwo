package cord.eoeo.momentwo.feature.auth.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class AuthDestinationsTest {
    @Test
    fun savedBackStackRouteIdentitiesRemainCompatible() {
        val prefix = "cord.eoeo.momentwo.core.navigation.MomentwoDestination."
        assertEquals(prefix + "Login", LoginDestination.serializer().descriptor.serialName)
        assertEquals(prefix + "SignUp", SignUpDestination.serializer().descriptor.serialName)
        assertEquals(prefix + "Profile", ProfileDestination.serializer().descriptor.serialName)
    }

    @Test
    fun profileArgumentsRoundTripWithAndWithoutNickname() {
        listOf(null, "fixture").forEach { nickname ->
            val destination = ProfileDestination(nickname)
            assertEquals(destination, Json.decodeFromString<ProfileDestination>(Json.encodeToString(destination)))
        }
    }
}
