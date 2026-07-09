package cord.eoeo.momentwo.core.data.comment.remote

import cord.eoeo.momentwo.core.data.comment.CommentDataSource
import cord.eoeo.momentwo.core.data.model.CommentPage
import cord.eoeo.momentwo.core.data.model.CreateComment
import cord.eoeo.momentwo.core.data.model.EditComment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommentRemoteDataSource(
    private val commentService: CommentService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CommentDataSource {
    override suspend fun createComment(createComment: CreateComment): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                commentService.postCreateComment(createComment)
            }
        }

    override suspend fun editComment(editComment: EditComment): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                commentService.putEditComment(editComment)
            }
        }

    override suspend fun deleteComment(
        albumId: Int,
        commentId: Int,
    ): Result<Unit> =
        runCatching {
            withContext(dispatcher) {
                commentService.deleteComment(albumId, commentId)
            }
        }

    override suspend fun getCommentPage(
        albumId: Int,
        photoId: Int,
        size: Int,
        cursor: Int,
    ): Result<CommentPage> =
        runCatching {
            withContext(dispatcher) {
                commentService.getCommentPage(albumId, photoId, size, cursor)
            }
        }
}
