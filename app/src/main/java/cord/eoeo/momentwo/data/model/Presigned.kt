package cord.eoeo.momentwo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PresignedRequest(
    val albumId: Int,
    @Json(name = "extension")
    val mimeType: String,
)

@JsonClass(generateAdapter = true)
data class PresignedUrl(
    val presignedUrl: String,
)
