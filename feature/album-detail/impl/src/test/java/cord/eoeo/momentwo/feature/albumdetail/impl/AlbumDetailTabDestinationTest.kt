package cord.eoeo.momentwo.feature.albumdetail.impl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class AlbumDetailTabDestinationTest {
    @Test fun savedInternalTabIdentitiesSurvivePackageMove() {
        val prefix = "cord.eoeo.momentwo.ui.albumdetail.AlbumDetailDestination."
        assertEquals(prefix + "Member", AlbumDetailTabDestination.Member.serializer().descriptor.serialName)
        assertEquals(prefix + "AlbumSetting", AlbumDetailTabDestination.AlbumSetting.serializer().descriptor.serialName)
        assertEquals(prefix + "ChangeImage", AlbumDetailTabDestination.ChangeImage.serializer().descriptor.serialName)
        assertEquals(AlbumDetailTabDestination.Member, Json.decodeFromString<AlbumDetailTabDestination.Member>(Json.encodeToString(AlbumDetailTabDestination.Member)))
        assertEquals(AlbumDetailTabDestination.AlbumSetting, Json.decodeFromString<AlbumDetailTabDestination.AlbumSetting>(Json.encodeToString(AlbumDetailTabDestination.AlbumSetting)))
        assertEquals(AlbumDetailTabDestination.ChangeImage, Json.decodeFromString<AlbumDetailTabDestination.ChangeImage>(Json.encodeToString(AlbumDetailTabDestination.ChangeImage)))
    }
}
