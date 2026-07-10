package cord.eoeo.momentwo.core.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadPhoto(
    val albumId: Int,
    val subAlbumId: Int,
    val filename: String,
)

@JsonClass(generateAdapter = true)
data class PhotoPage(
    val images: List<PhotoInfo>,
    val nextCursor: Int?,
)

@JsonClass(generateAdapter = true)
data class PhotoInfo(
    val id: Int,
    val imageUrl: String,
)

@JsonClass(generateAdapter = true)
data class LikedPhotoList(
    val likesList: List<LikedPhoto>,
)

@JsonClass(generateAdapter = true)
data class LikedPhoto(
    val albumId: Int,
    val subAlbumId: Int,
    val photoId: Int,
    val nickname: String,
    val likesStatus: Boolean,
)
