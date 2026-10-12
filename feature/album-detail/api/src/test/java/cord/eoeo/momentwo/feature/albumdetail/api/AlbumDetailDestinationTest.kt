package cord.eoeo.momentwo.feature.albumdetail.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class AlbumDetailDestinationTest {
    @Test fun savedIdentityAndAllArgumentsArePreserved() {
        assertEquals("cord.eoeo.momentwo.core.navigation.MomentwoDestination.AlbumDetail", AlbumDetailDestination.serializer().descriptor.serialName)
        val value = AlbumDetailDestination(123, "앨범 / 여행", "부제목? #", "")
        assertEquals(value, Json.decodeFromString<AlbumDetailDestination>(Json.encodeToString(value)))
    }
}
