package cord.eoeo.momentwo.core.data.model

import com.squareup.moshi.JsonClass
import cord.eoeo.momentwo.core.model.CommentItem

@JsonClass(generateAdapter = true)
data class CreateComment(
    val albumId: Int,
    val photoId: Int,
    val comments: String,
)

@JsonClass(generateAdapter = true)
data class EditComment(
    val albumId: Int,
    val commentId: Int,
    val editComments: String,
)

@JsonClass(generateAdapter = true)
data class CommentPage(
    val commentsList: List<Comment>,
    val nextCursor: Int?,
)

@JsonClass(generateAdapter = true)
data class Comment(
    val id: Int,
    val nickname: String,
    val userProfileImage: String,
    val comment: String,
    val date: String,
) {
    fun mapToCommentItem(): CommentItem =
        CommentItem(
            id = id,
            nickname = nickname,
            userProfileImage = userProfileImage,
            comment = comment,
            date = date,
        )
}
