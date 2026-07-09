package cord.eoeo.momentwo.core.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LikeRequest(
    val albumId: Int,
    val photoId: Int,
)

@JsonClass(generateAdapter = true)
data class LikeCount(
    val count: Int,
)
