package cord.eoeo.momentwo.core.model

data class DescriptionItem(
    val nickname: String,
    val userProfileImage: String,
    val description: String,
    val date: String,
    val photoTags: List<String>,
)
