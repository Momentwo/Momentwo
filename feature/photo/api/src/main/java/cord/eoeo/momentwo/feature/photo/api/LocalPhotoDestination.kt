package cord.eoeo.momentwo.feature.photo.api
import kotlinx.serialization.Serializable
@Serializable data class LocalPhotoListDestination(val subAlbumId: String)
@Serializable data class LocalPhotoDetailDestination(val localId: String)
