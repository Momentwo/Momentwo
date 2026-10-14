package cord.eoeo.momentwo.core.data.library
import org.junit.Test
import org.junit.Assert.*
class AlbumInvitationsTest {
    @Test fun previousJobsHaveNoInvitationsAndNamesRoundTripWithoutDelimiterLoss() {
        assertEquals(emptyList<String>(), AlbumInvitations.decode(""))
        val names = listOf("한글 친구", "a,b", "quote\"", "line\nname")
        assertEquals(names, AlbumInvitations.decode(AlbumInvitations.encode(names + names)))
    }
}
