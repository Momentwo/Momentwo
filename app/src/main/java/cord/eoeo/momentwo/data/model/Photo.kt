package cord.eoeo.momentwo.data.model

import com.squareup.moshi.JsonClass
import cord.eoeo.momentwo.ui.model.ImageItem

@JsonClass(generateAdapter = true)
data class DeletePhotos(
    val albumId: Int,
    val subAlbumId: Int,
    val imagesId: List<Int>,
    val imagesUrl: List<String>,
)

@JsonClass(generateAdapter = true)
data class PhotoPage(
    val images: List<PhotoInfo>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

@JsonClass(generateAdapter = true)
data class PhotoInfo(
    val id: Int,
    val imageUrl: String
) {
    fun mapToImageItem(): ImageItem =
        ImageItem(
            id = id,
            imageUrl = imageUrl,
        )
}
