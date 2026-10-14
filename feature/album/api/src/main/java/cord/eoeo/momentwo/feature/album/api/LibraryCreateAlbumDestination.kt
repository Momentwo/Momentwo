package cord.eoeo.momentwo.feature.album.api
import kotlinx.serialization.Serializable
@Serializable data class LibraryCreateAlbumDestination(val account: Boolean = false)
@Serializable data object AccountAlbumsDestination
