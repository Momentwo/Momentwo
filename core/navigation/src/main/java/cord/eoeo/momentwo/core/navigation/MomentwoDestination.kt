package cord.eoeo.momentwo.core.navigation

import kotlinx.serialization.Serializable

sealed interface MomentwoDestination {
    @Serializable
    data class PhotoList(
        val albumId: Int,
        val subAlbumId: Int,
        val albumTitle: String,
        val subAlbumTitle: String,
    ) : MomentwoDestination

    @Serializable
    data class PhotoDetail(
        val albumId: Int,
        val photoId: Int,
        val photoUrl: String,
        val isLiked: Boolean,
    ) : MomentwoDestination

}
