package cord.eoeo.momentwo.data.subalbum.remote

import cord.eoeo.momentwo.data.MomentwoApi
import cord.eoeo.momentwo.data.model.CreateSubAlbumInfo
import cord.eoeo.momentwo.data.model.EditSubAlbumInfo
import cord.eoeo.momentwo.data.model.SubAlbumIds
import cord.eoeo.momentwo.data.model.SubAlbumList
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SubAlbumService {
    @Headers("content-type: application/json")
    @POST(MomentwoApi.POST_CREATE_SUBALBUM)
    suspend fun postCreateSubAlbum(
        @Body createSubAlbumInfo: CreateSubAlbumInfo,
    )

    @Headers("content-type: application/json")
    @GET(MomentwoApi.GET_SUBALBUM_LIST)
    suspend fun getSubAlbumList(
        @Path("albumId") albumId: Int,
    ): SubAlbumList

    @Headers("content-type: application/json")
    @PUT(MomentwoApi.PUT_EDIT_SUBALBUM)
    suspend fun putEditSubAlbum(
        @Body editSubAlbumInfo: EditSubAlbumInfo,
    )

    @Headers("content-type: application/json")
    @DELETE(MomentwoApi.DELETE_SUBALBUMS)
    suspend fun deleteSubAlbums(
        @Body subAlbumIds: SubAlbumIds,
    )
}
