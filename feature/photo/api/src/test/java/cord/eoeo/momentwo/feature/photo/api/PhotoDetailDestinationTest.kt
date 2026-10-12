package cord.eoeo.momentwo.feature.photo.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class PhotoDetailDestinationTest {
    @Test fun savedIdentityAndAllArgumentsArePreserved() {
        assertEquals("cord.eoeo.momentwo.core.navigation.MomentwoDestination.PhotoDetail", PhotoDetailDestination.serializer().descriptor.serialName)
        val value = PhotoDetailDestination(123, 789, "https://example.invalid/photo?value=a%20b", true)
        assertEquals(value, Json.decodeFromString<PhotoDetailDestination>(Json.encodeToString(value)))
    }
}
