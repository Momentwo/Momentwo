package cord.eoeo.momentwo.feature.photo.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("cord.eoeo.momentwo.core.navigation.MomentwoDestination.PhotoDetail")
data class PhotoDetailDestination(
    val albumId: Int,
    val photoId: Int,
    val photoUrl: String,
    val isLiked: Boolean,
)
