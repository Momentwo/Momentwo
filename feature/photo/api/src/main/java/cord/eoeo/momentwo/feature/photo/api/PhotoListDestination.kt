package cord.eoeo.momentwo.feature.photo.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("cord.eoeo.momentwo.core.navigation.MomentwoDestination.PhotoList")
data class PhotoListDestination(
    val albumId: Int,
    val subAlbumId: Int,
    val albumTitle: String,
    val subAlbumTitle: String,
)
