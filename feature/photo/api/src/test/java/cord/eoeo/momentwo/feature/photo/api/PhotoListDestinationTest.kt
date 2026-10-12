package cord.eoeo.momentwo.feature.photo.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class PhotoListDestinationTest {
    @Test fun savedIdentityAndAllArgumentsArePreserved() {
        assertEquals("cord.eoeo.momentwo.core.navigation.MomentwoDestination.PhotoList", PhotoListDestination.serializer().descriptor.serialName)
        val value = PhotoListDestination(123, 456, "앨범 / 여행", "서브? #")
        assertEquals(value, Json.decodeFromString<PhotoListDestination>(Json.encodeToString(value)))
    }
}
