package cord.eoeo.momentwo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import cord.eoeo.momentwo.ui.model.ImageItem
import cord.eoeo.momentwo.ui.model.SubAlbumItem

@JsonClass(generateAdapter = true)
data class CreateSubAlbumInfo(
    val albumId: Int,
    val title: String,
)

@JsonClass(generateAdapter = true)
data class EditSubAlbumInfo(
    val albumId: Int,
    val subAlbumId: Int,
    @Json(name = "editTitle")
    val title: String,
)

@JsonClass(generateAdapter = true)
data class SubAlbumImage(
    val id: Int,
    val imageUrl: String,
) {
    fun mapToImageItem(): ImageItem =
        ImageItem(
            id = id,
            imageUrl = imageUrl,
        )
}

@JsonClass(generateAdapter = true)
data class SubAlbumInfo(
    val id: Int,
    @Json(name = "subAlbumTitle")
    val title: String,
    @Json(name = "subAlbumImageList")
    val imageList: List<SubAlbumImage>,
) {
    fun mapToSubAlbumItem(): SubAlbumItem =
        SubAlbumItem(
            id = id,
            title = title,
            imageList = imageList.map { it.mapToImageItem() },
        )
}

@JsonClass(generateAdapter = true)
data class SubAlbumList(
    val subAlbumList: List<SubAlbumInfo>,
)

@JsonClass(generateAdapter = true)
data class SubAlbumIds(
    val albumId: Int,
    val subAlbumIds: List<Int>,
)
