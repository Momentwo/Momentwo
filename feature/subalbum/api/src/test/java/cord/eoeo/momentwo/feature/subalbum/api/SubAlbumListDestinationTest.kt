package cord.eoeo.momentwo.feature.subalbum.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class SubAlbumListDestinationTest {
    @Test fun savedRouteIdentityAndRoundTripArePreserved() {
        assertEquals("cord.eoeo.momentwo.ui.albumdetail.AlbumDetailDestination.SubAlbumList", SubAlbumListDestination.serializer().descriptor.serialName)
        assertEquals(SubAlbumListDestination, Json.decodeFromString<SubAlbumListDestination>(Json.encodeToString(SubAlbumListDestination)))
    }
}
