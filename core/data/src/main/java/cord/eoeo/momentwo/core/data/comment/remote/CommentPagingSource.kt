package cord.eoeo.momentwo.core.data.comment.remote

import cord.eoeo.momentwo.core.data.CursorPagingSource
import cord.eoeo.momentwo.core.data.comment.CommentDataSource
import cord.eoeo.momentwo.core.data.model.Comment

class CommentPagingSource(
    private val commentRemoteDataSource: CommentDataSource,
    private val pageSize: Int,
    private val albumId: Int,
    private val photoId: Int,
) : CursorPagingSource<Comment>() {
    override suspend fun loadPage(nextCursor: Int): LoadResult<Int, Comment> {
        commentRemoteDataSource
            .getCommentPage(albumId, photoId, pageSize, nextCursor)
            .onSuccess { commentPage ->
                return LoadResult.Page(
                    data = commentPage.commentsList,
                    prevKey = null,
                    nextKey = commentPage.nextCursor,
                )
            }.onFailure { exception ->
                return LoadResult.Error(exception)
            }
        return LoadResult.Invalid()
    }
}
