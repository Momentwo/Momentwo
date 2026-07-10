package cord.eoeo.momentwo.core.data.model

import com.squareup.moshi.JsonClass
import cord.eoeo.momentwo.core.model.DescriptionItem

@JsonClass(generateAdapter = true)
data class CreateDescription(
    val albumId: Int,
    val photoId: Int,
    val description: String,
)

@JsonClass(generateAdapter = true)
data class EditDescription(
    val albumId: Int,
    val photoId: Int,
    val editDescription: String,
)

@JsonClass(generateAdapter = true)
data class Description(
    val nickname: String,
    val userProfileImage: String,
    val description: String,
    val date: String,
    val photoTags: List<String>,
) {
    fun mapToDescriptionItem(): DescriptionItem =
        DescriptionItem(
            nickname = nickname,
            userProfileImage = userProfileImage,
            description = description,
            date = date,
            photoTags = photoTags,
        )
}
