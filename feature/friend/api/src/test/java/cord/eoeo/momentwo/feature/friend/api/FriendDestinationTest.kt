package cord.eoeo.momentwo.feature.friend.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class FriendDestinationTest {
    @Test fun savedRouteIdentityAndRoundTripArePreserved() {
        assertEquals("cord.eoeo.momentwo.core.navigation.MomentwoDestination.Friend", FriendDestination.serializer().descriptor.serialName)
        assertEquals(FriendDestination, Json.decodeFromString<FriendDestination>(Json.encodeToString(FriendDestination)))
    }
}
