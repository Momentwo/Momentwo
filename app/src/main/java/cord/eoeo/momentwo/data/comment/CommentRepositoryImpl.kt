package cord.eoeo.momentwo.data.comment

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import cord.eoeo.momentwo.data.comment.remote.CommentPagingSource
import cord.eoeo.momentwo.data.model.CreateComment
import cord.eoeo.momentwo.data.model.EditComment
import cord.eoeo.momentwo.core.model.CommentItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CommentRepositoryImpl(
    private val commentRemoteDataSource: CommentDataSource,
) : CommentRepository {
    override suspend fun getCommentPagingData(
        albumId: Int,
        photoId: Int,
        pageSize: Int,
    ): Flow<PagingData<CommentItem>> =
        Pager(
            config = PagingConfig(pageSize = pageSize),
            pagingSourceFactory = {
                CommentPagingSource(
                    commentRemoteDataSource,
                    pageSize,
                    albumId,
                    photoId,
                )
            },
        ).flow.map { commentPagingData ->
            commentPagingData.map { it.mapToCommentItem() }
        }

    override suspend fun writeComment(
        albumId: Int,
        photoId: Int,
        comments: String,
    ): Result<Unit> = commentRemoteDataSource.createComment(CreateComment(albumId, photoId, comments))

    override suspend fun editComment(
        albumId: Int,
        commentId: Int,
        comments: String,
    ): Result<Unit> = commentRemoteDataSource.editComment(EditComment(albumId, commentId, comments))

    override suspend fun deleteComment(
        albumId: Int,
        commentId: Int,
    ): Result<Unit> = commentRemoteDataSource.deleteComment(albumId, commentId)
}
