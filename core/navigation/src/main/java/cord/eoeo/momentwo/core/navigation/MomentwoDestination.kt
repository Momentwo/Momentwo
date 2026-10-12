package cord.eoeo.momentwo.core.navigation

import kotlinx.serialization.Serializable

sealed interface MomentwoDestination {
    @Serializable
    data object Album : MomentwoDestination

    @Serializable
    data class AlbumDetail(
        val id: Int,
        val title: String,
        val subTitle: String,
        val imageUrl: String,
    ) : MomentwoDestination

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

    @Serializable
    data object CreateAlbum : MomentwoDestination

}
