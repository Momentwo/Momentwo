package cord.eoeo.momentwo.core.data.comment

import cord.eoeo.momentwo.core.data.model.CommentPage
import cord.eoeo.momentwo.core.data.model.CreateComment
import cord.eoeo.momentwo.core.data.model.EditComment

interface CommentDataSource {
    suspend fun createComment(createComment: CreateComment): Result<Unit>

    suspend fun editComment(editComment: EditComment): Result<Unit>

    suspend fun deleteComment(
        albumId: Int,
        commentId: Int,
    ): Result<Unit>

    suspend fun getCommentPage(
        albumId: Int,
        photoId: Int,
        size: Int,
        cursor: Int,
    ): Result<CommentPage>
}
