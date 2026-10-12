package cord.eoeo.momentwo.feature.album.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class AlbumDestinationTest {
    @Test fun savedRouteIdentityAndRoundTripArePreserved() {
        assertEquals("cord.eoeo.momentwo.core.navigation.MomentwoDestination.Album", AlbumDestination.serializer().descriptor.serialName)
        assertEquals(AlbumDestination, Json.decodeFromString<AlbumDestination>(Json.encodeToString(AlbumDestination)))
    }
}
