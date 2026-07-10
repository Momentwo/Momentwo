package cord.eoeo.momentwo.data.comment.remote

import cord.eoeo.momentwo.core.network.MomentwoApi
import cord.eoeo.momentwo.data.model.CommentPage
import cord.eoeo.momentwo.data.model.CreateComment
import cord.eoeo.momentwo.data.model.EditComment
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentService {
    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_CREATE_COMMENT)
    suspend fun postCreateComment(
        @Body createComment: CreateComment,
    )

    @Headers("content-type: application/json")
    @PUT(MomentwoApi.PUT_EDIT_COMMENT)
    suspend fun putEditComment(
        @Body editComment: EditComment,
    )

    @DELETE(MomentwoApi.DELETE_COMMENT)
    suspend fun deleteComment(
        @Path("albumId") albumId: Int,
        @Path("commentId") commentId: Int,
    )

    @GET(MomentwoApi.GET_COMMENT_PAGE)
    suspend fun getCommentPage(
        @Path("albumId") albumId: Int,
        @Path("photoId") photoId: Int,
        @Query("size") size: Int,
        @Query("cursor") cursor: Int,
    ): CommentPage
}
