package cord.eoeo.momentwo.feature.albumdetail.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("cord.eoeo.momentwo.core.navigation.MomentwoDestination.AlbumDetail")
data class AlbumDetailDestination(
    val id: Int,
    val title: String,
    val subTitle: String,
    val imageUrl: String,
)
