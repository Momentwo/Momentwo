package cord.eoeo.momentwo.core.data.comment

import androidx.paging.PagingData
import cord.eoeo.momentwo.core.model.CommentItem
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    suspend fun getCommentPagingData(
        albumId: Int,
        photoId: Int,
        pageSize: Int,
    ): Flow<PagingData<CommentItem>>

    suspend fun writeComment(
        albumId: Int,
        photoId: Int,
        comments: String,
    ): Result<Unit>

    suspend fun editComment(
        albumId: Int,
        commentId: Int,
        comments: String,
    ): Result<Unit>

    suspend fun deleteComment(
        albumId: Int,
        commentId: Int,
    ): Result<Unit>
}
