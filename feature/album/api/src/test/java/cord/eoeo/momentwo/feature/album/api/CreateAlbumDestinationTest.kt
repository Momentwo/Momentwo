package cord.eoeo.momentwo.feature.album.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class CreateAlbumDestinationTest {
    @Test fun savedRouteIdentityAndRoundTripArePreserved() {
        assertEquals("cord.eoeo.momentwo.core.navigation.MomentwoDestination.CreateAlbum", CreateAlbumDestination.serializer().descriptor.serialName)
        assertEquals(CreateAlbumDestination, Json.decodeFromString<CreateAlbumDestination>(Json.encodeToString(CreateAlbumDestination)))
    }
}
